/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.cloudsimplus.core;

import lombok.Getter;
import lombok.NonNull;
import org.cloudsimplus.core.events.SimEvent;
import org.cpnsim.datacenter.CollaborationManager;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.interscheduler.CenterSchedulerResult;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupEdge;
import org.cpnsim.request.UserRequest;
import org.cpnsim.statemanager.SimpleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A Cloud Information Service (CIS) is an entity that provides cloud resource
 * registration, indexing and discovery services. The Cloud datacenters tell their
 * readiness to process Cloudlets by registering themselves with this entity.
 * Other entities such as the broker can contact this class for
 * resource discovery service, which returns a list of registered resource.
 *
 * <p>
 * In summary, it acts like a yellow page service.
 * An instance of this class is automatically created by CloudSimPlus upon initialisation of the simulation.
 * </p>
 *
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @since CloudSim Toolkit 1.0
 */
public class CloudInformationService extends CloudSimEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudInformationService.class.getSimpleName());

    @Getter
    private final List<Datacenter> datacenterList;

    /**
     * Creates a new entity.
     *
     * @param simulation The CloudSimPlus instance that represents the simulation the Entity belongs to
     * @throws IllegalArgumentException when the entity name is invalid
     */
    public CloudInformationService(@NonNull Simulation simulation) {
        super(simulation);
        this.datacenterList = new ArrayList<>();
    }

    @Override
    protected void startInternal() {
        CollaborationManager collaborationManager = getSimulation().getCollaborationManager();
        if (collaborationManager.getIsChangeCollaborationSyn()) {
            send(this, collaborationManager.getChangeCollaborationSynTime(), CloudSimTag.CHANGE_COLLABORATION_SYN, null);
        }
    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case CloudSimTag.DC_REGISTRATION_REQUEST -> datacenterList.add((Datacenter) evt.getData());
            case CloudSimTag.DC_LIST_REQUEST -> super.send(evt.getSource(), 0, evt.getTag(), datacenterList);
            case CloudSimTag.USER_REQUEST_FAIL -> processUserRequestFail(evt);
            case CloudSimTag.CHANGE_COLLABORATION_SYN -> processChangeCollaborationSyn(evt);
            case CloudSimTag.USER_REQUEST_SEND -> processUserRequestSend(evt);
            case CloudSimTag.GROUP_FILTER_DC_BEGIN -> processGroupFilterDcBegin(evt);
            case CloudSimTag.RESPOND_SIMPLE_STATE -> processRespondSimpleState(evt);
            case CloudSimTag.GROUP_FILTER_DC_END -> processGroupFilterDcEnd(evt);
        }
    }


    private void processUserRequestSend(SimEvent evt) {
        if (evt.getData() instanceof List<?> userRequestsTmp) {
            CollaborationManager collaborationManager = getSimulation().getCollaborationManager();
            int collaborationId = -1;
            if (userRequestsTmp.size() == 0) {
                return;
            } else if (userRequestsTmp.get(0) instanceof UserRequest) {
                List<UserRequest> userRequests = (List<UserRequest>) userRequestsTmp;
                int dcId = userRequests.get(0).getBelongDatacenterId();
                collaborationId = collaborationManager.getOnlyCollaborationId(dcId);
                for (UserRequest userRequest : userRequests) {
                    if (userRequest.getState() != UserRequest.FAILED) {
                        collaborationManager.getCollaborationGroupQueueMap().get(collaborationId).add(userRequest);
                    }
                }
                LOGGER.info("{}: {} received {} user request.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), userRequests.size(), collaborationManager.getCollaborationGroupQueueMap().get(collaborationId).size());
            } else if (userRequestsTmp.get(0) instanceof InstanceGroup) {
                List<InstanceGroup> instanceGroups = (List<InstanceGroup>) userRequestsTmp;
                int dcId = instanceGroups.get(0).getUserRequest().getBelongDatacenterId();
                collaborationId = collaborationManager.getOnlyCollaborationId(dcId);

                for (InstanceGroup instanceGroup : instanceGroups) {
                    if (instanceGroup.getState() != UserRequest.FAILED) {
                        collaborationManager.getCollaborationGroupQueueMap().get(collaborationId).add(instanceGroup);
                    }
                }
                LOGGER.info("{}: {} received {} instance group.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), instanceGroups.size(), collaborationManager.getCollaborationGroupQueueMap().get(collaborationId).size());
            } else {
                LOGGER.error("{}: {} received an error data type,it is not List<UserRequest> or List<InstanceGroup>", getSimulation().clockStr(), getName());
                assert false;
            }
            if (!collaborationManager.getCenterSchedulerBusyMap().get(collaborationId)) {
                collaborationManager.getCenterSchedulerBusyMap().put(collaborationId, true);
                send(this, 0, CloudSimTag.GROUP_FILTER_DC_BEGIN, collaborationId);
            }
        }
    }

    private void processGroupFilterDcBegin(SimEvent evt) {
        if (evt.getData() instanceof Integer collaborationId) {
            CollaborationManager collaborationManager = getSimulation().getCollaborationManager();
            List<Datacenter> allDatacenters = collaborationManager.getDatacenters(collaborationId);
            InterScheduler interScheduler = collaborationManager.getCollaborationCenterSchedulerMap().get(collaborationId);
            for (Datacenter datacenter : allDatacenters) {
                sendOverNetwork(datacenter, 0, CloudSimTag.ASK_SIMPLE_STATE, null);
                interScheduler.getInterScheduleSimpleStateMap().put(datacenter, null);
            }
            LOGGER.info("{}: collaboration{}'s centerScheduler starts asking simple state for {} datacenters.", getSimulation().clockStr(), collaborationId, allDatacenters.size());
        }
    }

    private void processRespondSimpleState(final SimEvent evt) {
        if (evt.getData() != null) {
            int collaborationId = getSimulation().getCollaborationManager().getOnlyCollaborationId(evt.getSource().getId());
            InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
            interScheduler.getInterScheduleSimpleStateMap().put((Datacenter) evt.getSource(), evt.getData());
            if (interScheduler.getInterScheduleSimpleStateMap().containsValue(null)) {
                return;
            }

            List<InstanceGroup> instanceGroups = getSimulation().getCollaborationManager().getCollaborationGroupQueueMap().get(collaborationId).getBatchItem();
            LOGGER.info("{}: collaboration{}'s centerScheduler starts finding available Datacenters for {} instance groups.", getSimulation().clockStr(), collaborationId, instanceGroups.size());
            if (instanceGroups.size() == 0) {
                return;
            }
            Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = interScheduler.filterSuitableDatacenter(instanceGroups);
            CenterSchedulerResult centerSchedulerResult = new CenterSchedulerResult(collaborationId, instanceGroupAvailableDatacenters);
            double filterSuitableDatacenterCostTime = interScheduler.getFilterSuitableDatacenterCostTime();
            send(this, filterSuitableDatacenterCostTime, CloudSimTag.GROUP_FILTER_DC_END, centerSchedulerResult);
        }
    }

    private void processGroupFilterDcEnd(SimEvent evt) {
        if (evt.getData() instanceof CenterSchedulerResult centerSchedulerResult) {
            int collaborationId = centerSchedulerResult.getCollaborationId();
            CollaborationManager collaborationManager = getSimulation().getCollaborationManager();
            Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacentersMap = centerSchedulerResult.getInstanceGroupAvailableDatacentersMap();
            LOGGER.info("{}: collaboration{}'s centerScheduler ends finding available Datacenters for {} instance groups.", getSimulation().clockStr(), collaborationId, instanceGroupAvailableDatacentersMap.size());
            interScheduleByResult(instanceGroupAvailableDatacentersMap);
            if (collaborationManager.getCollaborationGroupQueueMap().get(collaborationId).size() > 0) {
                send(this, 0, CloudSimTag.GROUP_FILTER_DC_BEGIN, collaborationId);
            } else {
                collaborationManager.getCenterSchedulerBusyMap().put(collaborationId, false);
            }
        }
    }

    //根据筛选情况进行调度
    private void interScheduleByResult(Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters) {
        Map<Datacenter, List<InstanceGroup>> sendMap = new HashMap<>();//记录每个数据中心需要发送的亲和组以统一发送
        List<InstanceGroup> retryInstanceGroups = new ArrayList<>();
        for (Map.Entry<InstanceGroup, List<Datacenter>> entry : instanceGroupAvaiableDatacenters.entrySet()) {
            InstanceGroup instanceGroup = entry.getKey();
            List<Datacenter> datacenters = entry.getValue();
            if (datacenters.size() == 0) {
                //如果没有可调度的数据中心，那么就要么再次尝试要么设置为失败
                retryInstanceGroups.add(instanceGroup);
            } else {
                if (datacenters.size() > 1) {
                    LOGGER.error("{}: InstanceGroup{} has more than one datacenter.It is not allow in centerScheduling", getSimulation().clockStr(), instanceGroup.getId());
                    assert false;
                }
                //如果有可调度的数据中心，那么就将其发送给可调度的数据中心
                Datacenter datacenter = datacenters.get(0);
                if (sendMap.containsKey(datacenter)) {
                    sendMap.get(datacenter).add(instanceGroup);
                } else {
                    List<InstanceGroup> instanceGroups = new ArrayList<>();
                    instanceGroups.add(instanceGroup);
                    sendMap.put(datacenter, instanceGroups);
                }
            }
        }
        //向每个dc以list的形式发送instanceGroups
        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : sendMap.entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();
            for (InstanceGroup instanceGroup : instanceGroups) {
                instanceGroup.setReceiveDatacenter(datacenter);
                if (!allocateBwForGroup(instanceGroup, datacenter)) {
                    retryInstanceGroups.add(instanceGroup);
                    continue;
                }
                instanceGroup.setState(UserRequest.SCHEDULING);
            }
            sendOverNetwork(datacenter, 0, CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY, instanceGroups);
        }
        //处理调度失败的instanceGroup
        interScheduleFail(retryInstanceGroups);
    }


    private void interScheduleFail(List<InstanceGroup> instanceGroups) {
        List<InstanceGroup> retryInstanceGroups = new ArrayList<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            //如果重试次数增加了之后没有超过最大重试次数，那么就将其重新放入队列中等待下次调度
            instanceGroup.addRetryNum();
            if (instanceGroup.isFailed()) {
                instanceGroup.getUserRequest().setFailReason("InstanceGroup" + instanceGroup.getId());
                send(getSimulation().getCis(), 0, CloudSimTag.USER_REQUEST_FAIL, instanceGroup.getUserRequest());
            } else {
                retryInstanceGroups.add(instanceGroup);
            }
        }
        if (retryInstanceGroups.size() > 0) {
            send(this, 0, CloudSimTag.USER_REQUEST_SEND, retryInstanceGroups);
            LOGGER.warn("{}: {}'s {} instance groups retry.", getSimulation().clockStr(), getName(), retryInstanceGroups.size());
        }
    }


    private boolean allocateBwForGroup(InstanceGroup instanceGroup, Datacenter receiveDatacenter) {
        UserRequest userRequest = instanceGroup.getUserRequest();
        List<InstanceGroup> dstInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dst : dstInstanceGroups) {
            if (dst.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(instanceGroup, dst);
                if (!getSimulation().getNetworkTopology().allocateBw(receiveDatacenter, dst.getReceiveDatacenter(), edge.getRequiredBw())) {
                    return false;
                }
                //记录bw分配结果到数据库中
                getSimulation().getSqlRecord().recordInstanceGroupGraphAllocateInfo(receiveDatacenter.getId(), instanceGroup.getId(), dst.getReceiveDatacenter().getId(), dst.getId(), edge.getRequiredBw(), getSimulation().clock());
                userRequest.addAllocatedEdge(edge);
            }
        }
        List<InstanceGroup> srcInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup src : srcInstanceGroups) {
            if (src.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(src, instanceGroup);
                if (!getSimulation().getNetworkTopology().allocateBw(src.getReceiveDatacenter(), receiveDatacenter, edge.getRequiredBw())) {
                    return false;
                }//记录bw分配结果到数据库中
                getSimulation().getSqlRecord().recordInstanceGroupGraphAllocateInfo(src.getReceiveDatacenter().getId(), src.getId(), receiveDatacenter.getId(), instanceGroup.getId(), edge.getRequiredBw(), getSimulation().clock());
                userRequest.addAllocatedEdge(edge);
            }
        }
        return true;
    }

    public void processChangeCollaborationSyn(SimEvent evt) {
        CollaborationManager collaborationManager = getSimulation().getCollaborationManager();
        collaborationManager.changeCollaboration();
        if (collaborationManager.getIsChangeCollaborationSyn()) {
            send(this, collaborationManager.getChangeCollaborationSynTime(), CloudSimTag.CHANGE_COLLABORATION_SYN, null);
        }
    }

    private void processUserRequestFail(SimEvent evt) {
        if (evt.getData() instanceof UserRequest userRequest) {
            if (userRequest.getState() == UserRequest.FAILED) {
                return;
            }
            LOGGER.warn("{}: The UserRequest{} has failed. Reason: {}", getSimulation().clockStr(), userRequest.getId(), userRequest.getFailReason());
            userRequest.setState(UserRequest.FAILED);
            userRequest.setFinishTime(getSimulation().clock());
            getSimulation().getSqlRecord().recordUserRequestFinishInfo(userRequest);
            //释放Bw资源
            List<InstanceGroupEdge> allocateEdges = userRequest.getAllocatedEdges();
            for (InstanceGroupEdge allocateEdge : allocateEdges) {
                double allocatedBw = allocateEdge.getRequiredBw();
                Datacenter src = allocateEdge.getSrc().getReceiveDatacenter();
                Datacenter dest = allocateEdge.getDst().getReceiveDatacenter();
                if (src != null && dest != null) {
                    getSimulation().getNetworkTopology().releaseBw(src, dest, allocatedBw);
                }
            }
            //释放主机资源,结束已经在运行的任务,并且记录未运行的instance
            List<Instance> recordInstances = new ArrayList<>();
            for (InstanceGroup instanceGroup : userRequest.getInstanceGroups()) {
                if (instanceGroup.getState() == UserRequest.SCHEDULING) {
                    getSimulation().getSqlRecord().recordInstanceGroupFinishInfo(instanceGroup);
                } else {
                    getSimulation().getSqlRecord().recordInstanceGroupAllInfo(instanceGroup);
                }
                instanceGroup.setState(UserRequest.FAILED);
                instanceGroup.setFinishTime(getSimulation().clock());
                Map<Datacenter, List<Instance>> endInstances = new HashMap<>();
                for (Instance instance : instanceGroup.getInstanceList()) {
                    if (instance.getState() == UserRequest.RUNNING) {
                        Datacenter placedDc = instance.getInstanceGroup().getReceiveDatacenter();
                        if (!endInstances.containsKey(placedDc)) {
                            endInstances.put(placedDc, new ArrayList<>());
                        }
                        endInstances.put(placedDc, new ArrayList<>());
                    } else {
                        instance.setState(UserRequest.FAILED);
                        instance.setFinishTime(getSimulation().clock());
                        recordInstances.add(instance);
                    }
                }
                for (Map.Entry<Datacenter, List<Instance>> entry : endInstances.entrySet()) {
                    Datacenter datacenter = entry.getKey();
                    List<Instance> instances = entry.getValue();
                    send(datacenter, 0, CloudSimTag.END_INSTANCE_RUN, instances);
                }
            }
            getSimulation().getSqlRecord().recordInstancesAllInfo(recordInstances);
        }
    }
}