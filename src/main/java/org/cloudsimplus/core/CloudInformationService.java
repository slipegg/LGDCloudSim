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
import org.cpnsim.interscheduler.InterSchedulerResult;
import org.cpnsim.interscheduler.InterSchedulerSimple;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupEdge;
import org.cpnsim.request.UserRequest;
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

//    private Map<Integer, Map<Datacenter, Boolean>> interSchedulerRepliesWaitingMap = new HashMap<>();

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
            sendWithoutNetwork(this, collaborationManager.getChangeCollaborationSynTime(), CloudSimTag.CHANGE_COLLABORATION_SYN, null);
        }

        for (InterScheduler interScheduler : collaborationManager.getCollaborationCenterSchedulerMap().values()) {
            Map<Double, List<Datacenter>> initSynStateBetweenDcTargets = divideDcOnSynGap(interScheduler);

            if (!initSynStateBetweenDcTargets.isEmpty()) {
                for (Map.Entry<Double, List<Datacenter>> entry : initSynStateBetweenDcTargets.entrySet()) {
                    sendWithoutNetwork(this, 0, CloudSimTag.SYN_STATE_BETWEEN_DC, entry.getValue());
                }
            }
        }
    }

    private Map<Double, List<Datacenter>> divideDcOnSynGap(InterScheduler interScheduler) {
        Map<Double, List<Datacenter>> synStateBetweenDcTargets = new HashMap<>();

        for (Map.Entry<Datacenter, Double> synStateBetweenDcGaps : interScheduler.getDcStateSynInterval().entrySet()) {
            Datacenter datacenter = synStateBetweenDcGaps.getKey();
            double synGap = synStateBetweenDcGaps.getValue();

            if (synGap != 0) {
                synStateBetweenDcTargets.putIfAbsent(synGap, new ArrayList<>());
                synStateBetweenDcTargets.get(synGap).add(datacenter);
            }
        }

        return synStateBetweenDcTargets;
    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case CloudSimTag.SYN_STATE_BETWEEN_DC -> processSynStateBetweenDc(evt);
            case CloudSimTag.DC_REGISTRATION_REQUEST -> datacenterList.add((Datacenter) evt.getData());
            case CloudSimTag.DC_LIST_REQUEST -> super.send(evt.getSource(), 0, evt.getTag(), datacenterList);
            case CloudSimTag.USER_REQUEST_FAIL -> processUserRequestFail(evt);
            case CloudSimTag.CHANGE_COLLABORATION_SYN -> processChangeCollaborationSyn(evt);
            case CloudSimTag.USER_REQUEST_SEND -> processUserRequestSend(evt);
            case CloudSimTag.GROUP_FILTER_DC_BEGIN -> processGroupFilterDcBegin(evt);
            case CloudSimTag.GROUP_FILTER_DC_END -> processGroupFilterDcEnd(evt);
            case CloudSimTag.SCHEDULE_TO_DC_HOST_OK, CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED ->
                    processScheduleToDcHostResponse(evt);
        }
    }

    private void processSynStateBetweenDc(SimEvent evt) {
        if (evt.getData() instanceof List<?> synTargets) {
            if (!synTargets.isEmpty() && synTargets.get(0) instanceof Datacenter) {
                List<Datacenter> datacenters = (List<Datacenter>) synTargets;
                int collaborationId = getSimulation().getCollaborationManager().getOnlyCollaborationId(datacenters.get(0).getId());
                InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
                interScheduler.synBetweenDcState(datacenters);
                //TODO 需要考虑中途同步时间是否会变，会变的话需要检查，如果一直都不会变，就不改了
                sendWithoutNetwork(this, interScheduler.getDcStateSynInterval().get(datacenters.get(0)), CloudSimTag.SYN_STATE_BETWEEN_DC, datacenters);
            }
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
                InterScheduler interScheduler = collaborationManager.getCollaborationCenterSchedulerMap().get(collaborationId);

                interScheduler.addUserRequests(userRequests);

                LOGGER.info("{}: {} received {} user request.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), userRequests.size(), interScheduler.getNewQueueSize());
            } else if (userRequestsTmp.get(0) instanceof InstanceGroup) {
                List<InstanceGroup> instanceGroups = (List<InstanceGroup>) userRequestsTmp;
                int dcId = instanceGroups.get(0).getUserRequest().getBelongDatacenterId();
                collaborationId = collaborationManager.getOnlyCollaborationId(dcId);
                InterScheduler interScheduler = collaborationManager.getCollaborationCenterSchedulerMap().get(collaborationId);

                interScheduler.addInstanceGroups(instanceGroups, false);

                LOGGER.info("{}: {} received {} instance group.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), instanceGroups.size(), interScheduler.getNewQueueSize());
            } else {
                throw new RuntimeException(String.format("%s: %s received an error data type,it is not List<UserRequest> or List<InstanceGroup>", getSimulation().clockStr(), getName()));
            }

            if (!collaborationManager.getCenterSchedulerBusyMap().containsKey(collaborationId) ||
                    (!collaborationManager.getCenterSchedulerBusyMap().get(collaborationId) && !collaborationManager.getCollaborationCenterSchedulerMap().get(collaborationId).isQueuesEmpty())) {
                collaborationManager.getCenterSchedulerBusyMap().put(collaborationId, true);
                send(this, 0, CloudSimTag.GROUP_FILTER_DC_BEGIN, collaborationId);
            }
        }
    }

    private void processScheduleToDcHostResponse(SimEvent evt) {
        Datacenter sourceDc = (Datacenter) evt.getSource();
        int collaborationId = getSimulation().getCollaborationManager().getOnlyCollaborationId(sourceDc.getId());
        InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
        interScheduler.receiveReplyFromDatacenter(sourceDc);

        if (evt.getTag() == CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED) {
            List<InstanceGroup> failedInstanceGroups = (List<InstanceGroup>) evt.getData();
            addFailedInstanceGroupsByCollaborationId(collaborationId, failedInstanceGroups);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}: {}'s {} failed to schedule {} instanceGroups,it need retry soon.", getSimulation().clockStr(), getName(), interScheduler.getName(), failedInstanceGroups.size());
            }
        }

        if (interScheduler.isAllReplyReceived()) {
            LOGGER.info("{}: collaboration{}'s centerScheduler received all replies.", getSimulation().clockStr(), collaborationId);

            startCenterInterScheduling(collaborationId);
        }
    }

    private void markInstanceGroups(List<InstanceGroup> instanceGroups, Datacenter datacenter) {
        for (InstanceGroup instanceGroup : instanceGroups) {
            instanceGroup.setState(UserRequest.SCHEDULING);
            instanceGroup.setReceiveDatacenter(datacenter);
        }
    }

    private List<InstanceGroup> allocateBwForInstanceGroups(List<InstanceGroup> instanceGroups, Datacenter datacenter) {
        List<InstanceGroup> failedInstanceGroups = new ArrayList<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            instanceGroup.setReceiveDatacenter(datacenter);
            if (!allocateBwForGroup(instanceGroup, datacenter)) {
                failedInstanceGroups.add(instanceGroup);
            }
        }
        return failedInstanceGroups;
    }

    private void addFailedInstanceGroupsByCollaborationId(int collaborationId, List<InstanceGroup> failedInstanceGroups) {
        InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
        interScheduler.addInstanceGroups(failedInstanceGroups, true);
    }

    private void startCenterInterScheduling(int collaborationId) {
        InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
        if (interScheduler.isQueuesEmpty()) {
            getSimulation().getCollaborationManager().getCenterSchedulerBusyMap().put(collaborationId, false);
        } else {
            send(this, 0, CloudSimTag.GROUP_FILTER_DC_BEGIN, collaborationId);
        }
    }

    private void processGroupFilterDcBegin(SimEvent evt) {
        if (evt.getData() instanceof Integer collaborationId) {
            CollaborationManager collaborationManager = getSimulation().getCollaborationManager();
            InterScheduler interScheduler = collaborationManager.getCollaborationCenterSchedulerMap().get(collaborationId);

            LOGGER.info("{}: collaboration{}'s centerScheduler starts scheduling.", getSimulation().clockStr(), collaborationId);

            InterSchedulerResult interSchedulerResult = interScheduler.schedule();

            double scheduleTime = interScheduler.getScheduleTime();
            send(this, scheduleTime, CloudSimTag.GROUP_FILTER_DC_END, interSchedulerResult);
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
            double filterSuitableDatacenterCostTime = interScheduler.getScheduleTime();
            send(this, filterSuitableDatacenterCostTime, CloudSimTag.GROUP_FILTER_DC_END, centerSchedulerResult);
        }
    }

    private void processGroupFilterDcEnd(SimEvent evt) {
        if (evt.getData() instanceof InterSchedulerResult interSchedulerResult) {
            int collaborationId = interSchedulerResult.getCollaborationId();

            if (interSchedulerResult.getTarget() == InterSchedulerSimple.DC_TARGET && !interSchedulerResult.getIsSupportForward()) {
                allocateBwForInterSchedulerResult(interSchedulerResult);
            }

            sendInterScheduleResult(interSchedulerResult);

            handleFailedInterScheduling(interSchedulerResult);

            if (interSchedulerResult.getTarget() == InterSchedulerSimple.DC_TARGET
                    || interSchedulerResult.isScheduledInstanceGroupsEmpty()) {
                startCenterInterScheduling(collaborationId);
            }

            LOGGER.info("{}: collaboration{}'s centerScheduler ends finding available Datacenters for {} instanceGroups.", getSimulation().clockStr(), collaborationId, interSchedulerResult.getInstanceGroupNum());
        }
    }

    private void allocateBwForInterSchedulerResult(InterSchedulerResult interSchedulerResult) {
        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : interSchedulerResult.getScheduledResultMap().entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();
            for (InstanceGroup instanceGroup : instanceGroups) {
                instanceGroup.setReceiveDatacenter(datacenter);
                if (!allocateBwForGroup(instanceGroup, datacenter)) {
                    interSchedulerResult.addFailedInstanceGroup(instanceGroup);
                } else {
                    instanceGroup.setState(UserRequest.SCHEDULING);
                }
            }
        }
    }

    private void sendInterScheduleResult(InterSchedulerResult interSchedulerResult) {
        int evtTag = getEvtTagByInterSchedulerResult(interSchedulerResult);
        InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(interSchedulerResult.getCollaborationId());

        if (evtTag == CloudSimTag.SCHEDULE_TO_DC_HOST) {
            interScheduler.clearReplyWaitingDatacenter();
        }

        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : interSchedulerResult.getScheduledResultMap().entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();

            if (instanceGroups.size() > 0) {
                send(datacenter, 0, evtTag, instanceGroups);

                if (evtTag == CloudSimTag.SCHEDULE_TO_DC_HOST) {
                    interScheduler.addReplyWaitingDatacenter(datacenter);
                }
            }
        }
    }

    private int getEvtTagByInterSchedulerResult(InterSchedulerResult interSchedulerResult) {
        if (interSchedulerResult.getTarget() == InterSchedulerSimple.DC_TARGET) {
            if (interSchedulerResult.getIsSupportForward()) {
                return CloudSimTag.SCHEDULE_TO_DC_AND_FORWARD;
            } else {
                return CloudSimTag.SCHEDULE_TO_DC_NO_FORWARD;
            }
        } else if (interSchedulerResult.getTarget() == InterSchedulerSimple.HOST_TARGET) {
            return CloudSimTag.SCHEDULE_TO_DC_HOST;
        } else {
            throw new RuntimeException(String.format("%s: %s received an error target,it is not DC_TARGET or HOST_TARGET", getSimulation().clockStr(), getName()));
        }
    }

    //根据筛选情况进行调度
//    private void InterScheduleByResult(InterSchedulerResult interSchedulerResult) {
//        Map<Datacenter, List<InstanceGroup>> sendMap = new HashMap<>();//记录每个数据中心需要发送的亲和组以统一发送
//        List<InstanceGroup> retryInstanceGroups = new ArrayList<>();
//        for (Map.Entry<InstanceGroup, List<Datacenter>> entry : instanceGroupAvaiableDatacenters.entrySet()) {
//            InstanceGroup instanceGroup = entry.getKey();
//            List<Datacenter> datacenters = entry.getValue();
//            if (datacenters.size() == 0) {
//                //如果没有可调度的数据中心，那么就要么再次尝试要么设置为失败
//                retryInstanceGroups.add(instanceGroup);
//            } else {
//                if (datacenters.size() > 1) {
//                    LOGGER.error("{}: InstanceGroup{} has more than one datacenter.It is not allow in centerScheduling", getSimulation().clockStr(), instanceGroup.getId());
//                    assert false;
//                }
//                //如果有可调度的数据中心，那么就将其发送给可调度的数据中心
//                Datacenter datacenter = datacenters.get(0);
//                if (sendMap.containsKey(datacenter)) {
//                    sendMap.get(datacenter).add(instanceGroup);
//                } else {
//                    List<InstanceGroup> instanceGroups = new ArrayList<>();
//                    instanceGroups.add(instanceGroup);
//                    sendMap.put(datacenter, instanceGroups);
//                }
//            }
//        }
//        //向每个dc以list的形式发送instanceGroups
//        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : sendMap.entrySet()) {
//            Datacenter datacenter = entry.getKey();
//            List<InstanceGroup> instanceGroups = entry.getValue();
//            for (InstanceGroup instanceGroup : instanceGroups) {
//                instanceGroup.setReceiveDatacenter(datacenter);
//                if (!allocateBwForGroup(instanceGroup, datacenter)) {
//                    retryInstanceGroups.add(instanceGroup);
//                    continue;
//                }
//                instanceGroup.setState(UserRequest.SCHEDULING);
//            }
//            sendOverNetwork(datacenter, 0, CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY, instanceGroups);
//        }
//        //处理调度失败的instanceGroup
//        interScheduleFail(retryInstanceGroups);
//    }


    private void handleFailedInterScheduling(InterSchedulerResult interSchedulerResult) {
        List<InstanceGroup> retryInstanceGroups = new ArrayList<>();
        Set<UserRequest> failedUserRequests = new HashSet<>();
        List<InstanceGroup> failedInstanceGroups = interSchedulerResult.getFailedInstanceGroups();

        for (InstanceGroup instanceGroup : failedInstanceGroups) {
            //如果重试次数增加了之后没有超过最大重试次数，那么就将其重新放入队列中等待下次调度
            instanceGroup.addRetryNum();

            if (instanceGroup.isFailed()) {
                instanceGroup.getUserRequest().addFailReason("InstanceGroup" + instanceGroup.getId());

                failedUserRequests.add(instanceGroup.getUserRequest());
            } else {
                retryInstanceGroups.add(instanceGroup);
            }
        }

        if (retryInstanceGroups.size() > 0) {
            int collaborationId = interSchedulerResult.getCollaborationId();
            InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
            interScheduler.addInstanceGroups(retryInstanceGroups, true);
        }

        if (failedUserRequests.size() > 0) {
            send(this, 0, CloudSimTag.USER_REQUEST_FAIL, failedUserRequests);
            LOGGER.warn("{}: {}'s {} user requests failed.", getSimulation().clockStr(), getName(), failedUserRequests.size());
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
            sendWithoutNetwork(this, collaborationManager.getChangeCollaborationSynTime(), CloudSimTag.CHANGE_COLLABORATION_SYN, null);
        }
    }

    private void processUserRequestFail(SimEvent evt) {
        if (evt.getData() instanceof Set<?> userRequestsTmp) {
            if (userRequestsTmp.size() > 0 && userRequestsTmp.iterator().next() instanceof UserRequest) {
                Set<UserRequest> userRequests = (Set<UserRequest>) userRequestsTmp;
                for (UserRequest userRequest : userRequests) {
                    processAUserRequestFail(userRequest);
                }
            }
        }
    }

    private void processAUserRequestFail(UserRequest userRequest) {
        if (userRequest.getState() == UserRequest.FAILED) {
            return;
        }

        LOGGER.warn("{}: The UserRequest{} has failed. Reason: {}", getSimulation().clockStr(), userRequest.getId(), userRequest.getFailReason());

        markUserRequestFailedAndRecord(userRequest);

        releaseBwForFailedUserRequest(userRequest);

        releaseHostResourceForFailedUserRequest(userRequest);
    }

    private void markUserRequestFailedAndRecord(UserRequest userRequest) {
        userRequest.setState(UserRequest.FAILED);
        userRequest.setFinishTime(getSimulation().clock());
        getSimulation().getSqlRecord().recordUserRequestFinishInfo(userRequest);
    }

    private void releaseBwForFailedUserRequest(UserRequest userRequest) {
        List<InstanceGroupEdge> allocateEdges = userRequest.getAllocatedEdges();
        for (InstanceGroupEdge allocateEdge : allocateEdges) {
            double allocatedBw = allocateEdge.getRequiredBw();
            Datacenter src = allocateEdge.getSrc().getReceiveDatacenter();
            Datacenter dest = allocateEdge.getDst().getReceiveDatacenter();
            if (src != null && dest != null) {
                getSimulation().getNetworkTopology().releaseBw(src, dest, allocatedBw);
            }
        }
    }

    private void releaseHostResourceForFailedUserRequest(UserRequest userRequest) {
        Map<Datacenter, List<Instance>> endInstances = new HashMap<>();
        List<Instance> recordInstances = new ArrayList<>();

        for (InstanceGroup instanceGroup : userRequest.getInstanceGroups()) {
            markInstanceGroupFailedAndRecord(instanceGroup);

            addRecordInstancesAndEndInstancesByInstanceGroup(recordInstances, endInstances, instanceGroup);
        }

        for (Map.Entry<Datacenter, List<Instance>> endInstancesEntry : endInstances.entrySet()) {
            Datacenter datacenter = endInstancesEntry.getKey();
            List<Instance> instances = endInstancesEntry.getValue();

            send(datacenter, 0, CloudSimTag.END_INSTANCE_RUN, instances);
        }

        getSimulation().getSqlRecord().recordInstancesAllInfo(recordInstances);
    }

    private void markInstanceGroupFailedAndRecord(InstanceGroup instanceGroup) {
        instanceGroup.setState(UserRequest.FAILED);
        instanceGroup.setFinishTime(getSimulation().clock());

        if (instanceGroup.getState() == UserRequest.SCHEDULING) {
            getSimulation().getSqlRecord().recordInstanceGroupFinishInfo(instanceGroup);
        } else {
            getSimulation().getSqlRecord().recordInstanceGroupAllInfo(instanceGroup);
        }
    }

    private void addRecordInstancesAndEndInstancesByInstanceGroup(List<Instance> recordInstances, Map<Datacenter, List<Instance>> endInstances, InstanceGroup instanceGroup) {
        for (Instance instance : instanceGroup.getInstances()) {
            if (instance.getState() == UserRequest.RUNNING) {
                Datacenter placedDc = instance.getInstanceGroup().getReceiveDatacenter();
                endInstances.putIfAbsent(placedDc, new ArrayList<>());
                endInstances.get(placedDc).add(instance);
            } else {
                instance.setState(UserRequest.FAILED);
                instance.setFinishTime(getSimulation().clock());
                recordInstances.add(instance);
            }
        }
    }
}