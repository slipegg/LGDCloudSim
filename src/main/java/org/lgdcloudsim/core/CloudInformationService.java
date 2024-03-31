/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
package org.lgdcloudsim.core;

import lombok.Getter;
import lombok.NonNull;
import org.lgdcloudsim.core.events.SimEvent;
import org.lgdcloudsim.datacenter.CollaborationManager;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.util.FailedOutdatedResult;
import org.lgdcloudsim.interscheduler.InterScheduler;
import org.lgdcloudsim.interscheduler.InterSchedulerResult;
import org.lgdcloudsim.interscheduler.InterSchedulerSimple;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceGroupEdge;
import org.lgdcloudsim.request.UserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A Cloud Information Service (CIS) also named as cloud administrator, It is the overall administrator of the system and the cloud administrator of each collaboration zone.
 * Its functions include managing and registering all data center information,
 * managing the scope of each collaboration area through the Collaboration Manager,
 * and performing upper-layer centralized inter-data center scheduling in the collaboration area by maintaining the inter-scheduler of each collaboration area.
 *
 * @author Anonymous
 * @since LGDCSim 1.0
 */
public class CloudInformationService extends CloudSimEntity {
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudInformationService.class.getSimpleName());

    /**
     * The list of data centers that are registered in the system.
     */
    @Getter
    private final List<Datacenter> datacenterList;

    /**
     * Creates a new CIS entity.
     *
     * @param simulation The CloudSimPlus instance that represents the simulation the Entity belongs to
     * @throws IllegalArgumentException when the entity name is invalid
     */
    public CloudInformationService(@NonNull Simulation simulation) {
        super(simulation);
        this.datacenterList = new ArrayList<>();
    }

    /**
     * Starts the CIS entity.
     * If necessary, it will send a message to itself to start the process of changing the collaboration periodically
     * and to synchronize the state between data centers.
     */
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

    /**
     * Calculates the synchronization gap between data centers of the inter-scheduler.
     *
     * @param interScheduler the inter-scheduler to calculate the synchronization gap
     * @return a map where the key is the synchronization gap and the value is a list of data centers that have the same synchronization gap
     */
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


    /**
     * The events that the CIS needs to process.
     * @see SimEntity#processEvent(SimEvent)
     * @param evt the event
     */
    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case CloudSimTag.SYN_STATE_BETWEEN_DC -> processSynStateBetweenDc(evt);
            case CloudSimTag.DC_REGISTRATION_REQUEST -> datacenterList.add((Datacenter) evt.getData());
            case CloudSimTag.DC_LIST_REQUEST -> super.send(evt.getSource(), 0, evt.getTag(), datacenterList);
            case CloudSimTag.USER_REQUEST_FAIL -> processUserRequestFail(evt);
            case CloudSimTag.CHANGE_COLLABORATION_SYN -> processChangeCollaborationSyn(evt);
            case CloudSimTag.USER_REQUEST_SEND -> processUserRequestSend(evt);
            case CloudSimTag.INTER_SCHEDULE_BEGIN -> processInterScheduleBegin(evt);
            case CloudSimTag.INTER_SCHEDULE_END -> processInterScheduleEnd(evt);
            case CloudSimTag.SCHEDULE_TO_DC_HOST_OK, CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED ->
                    processScheduleToDcHostResponse(evt);
        }
    }

    /**
     * Synchronizes the state between data centers of the inter-scheduler.
     * @param evt the event
     */
    private void processSynStateBetweenDc(SimEvent evt) {
        if (evt.getData() instanceof List<?> synTargets) {
            if (!synTargets.isEmpty() && synTargets.get(0) instanceof Datacenter) {
                List<Datacenter> datacenters = (List<Datacenter>) synTargets;
                int collaborationId = getSimulation().getCollaborationManager().getOnlyCollaborationId(datacenters.get(0).getId());
                InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
                interScheduler.synBetweenDcState(datacenters);
                //TODO Need to consider whether the synchronization time will change midway. If it does, we need to check it. If it never changes, don't change it.
                sendWithoutNetwork(this, interScheduler.getDcStateSynInterval().get(datacenters.get(0)), CloudSimTag.SYN_STATE_BETWEEN_DC, datacenters);
            }
        }
    }

    /**
     * Processes the user request to be sent.
     * It will send all received user requests to the inter-scheduler.
     * @param evt the event
     */
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
                send(this, 0, CloudSimTag.INTER_SCHEDULE_BEGIN, collaborationId);
            }
        }
    }

    /**
     * Processes the response of scheduling to the host of the data center.
     * If some instance groups are conflicted, it will handle the failed instance groups.
     * @param evt the event
     */
    private void processScheduleToDcHostResponse(SimEvent evt) {
        Datacenter sourceDc = (Datacenter) evt.getSource();
        int collaborationId = getSimulation().getCollaborationManager().getOnlyCollaborationId(sourceDc.getId());

        if (evt.getTag() == CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED) {
            FailedOutdatedResult<InstanceGroup> failedOutdatedResult = (FailedOutdatedResult<InstanceGroup>) evt.getData();
            handleFailedInterScheduling(collaborationId, failedOutdatedResult.getFailRes(), failedOutdatedResult.getOutdatedRequests());
        }
    }

    /**
     * Starts the inter-scheduling of the center inter-scheduler in the collaboration zone with the given id.
     * @param collaborationId the collaboration id
     */
    private void startCenterInterScheduling(int collaborationId) {
        InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
        if (interScheduler.isQueuesEmpty()) {
            getSimulation().getCollaborationManager().getCenterSchedulerBusyMap().put(collaborationId, false);
        } else {
            send(this, 0, CloudSimTag.INTER_SCHEDULE_BEGIN, collaborationId);
        }
    }

    /**
     * Processes the beginning of the inter-scheduling.
     * The data of the evt should be the collaboration id.
     *
     * @param evt the event
     */
    private void processInterScheduleBegin(SimEvent evt) {
        if (evt.getData() instanceof Integer collaborationId) {
            CollaborationManager collaborationManager = getSimulation().getCollaborationManager();
            InterScheduler interScheduler = collaborationManager.getCollaborationCenterSchedulerMap().get(collaborationId);

            InterSchedulerResult interSchedulerResult = interScheduler.schedule();

            double scheduleTime = interScheduler.getScheduleTime();
            send(this, scheduleTime, CloudSimTag.INTER_SCHEDULE_END, interSchedulerResult);
            LOGGER.info("{}: collaboration{}'s centerScheduler starts scheduling.It will cost {}ms", getSimulation().clockStr(), collaborationId, scheduleTime);
        }
    }

    /**
     * Processes the end of the inter-scheduling.
     * It will send the result of the inter-scheduling to the data centers.
     *
     * @param evt the event
     */
    private void processInterScheduleEnd(SimEvent evt) {
        if (evt.getData() instanceof InterSchedulerResult interSchedulerResult) {
            int collaborationId = interSchedulerResult.getCollaborationId();

            if (interSchedulerResult.getTarget() == InterSchedulerSimple.DC_TARGET && !interSchedulerResult.getIsSupportForward()) {
                allocateBwForInterSchedulerResult(interSchedulerResult);
            }

            sendInterScheduleResult(interSchedulerResult);

            handleFailedInterScheduling(interSchedulerResult.getCollaborationId(), interSchedulerResult.getFailedInstanceGroups(), interSchedulerResult.getOutDatedUserRequests());

            startCenterInterScheduling(collaborationId);

            LOGGER.info("{}: collaboration{}'s centerScheduler ends finding available Datacenters for {} instanceGroups.", getSimulation().clockStr(), collaborationId, interSchedulerResult.getInstanceGroupNum());
        }
    }

    /**
     * Allocates the bandwidth for the inter-scheduler result.
     * @param interSchedulerResult the result of the inter-scheduler
     */
    //TODO: Check if there is a problem with bandwidth allocated elsewhere.
    private void allocateBwForInterSchedulerResult(InterSchedulerResult interSchedulerResult) {
        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : interSchedulerResult.getScheduledResultMap().entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();
            List<InstanceGroup> groupsToRemove = new ArrayList<>();
            for (InstanceGroup instanceGroup : instanceGroups) {
                if (!allocateBwForGroup(instanceGroup, datacenter)) {
                    interSchedulerResult.addFailedInstanceGroup(instanceGroup);
                    groupsToRemove.add(instanceGroup);
                } else {
                    instanceGroup.setState(UserRequest.SCHEDULING);
                    instanceGroup.setReceiveDatacenter(datacenter);
                }
            }
            instanceGroups.removeAll(groupsToRemove);
        }
    }

    /**
     * Sends the result of the inter-scheduling to the data centers.
     * @param interSchedulerResult the result of the inter-scheduling
     */
    private void sendInterScheduleResult(InterSchedulerResult interSchedulerResult) {
        int evtTag = getEvtTagByInterSchedulerResult(interSchedulerResult);

        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : interSchedulerResult.getScheduledResultMap().entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();

            if (instanceGroups.size() > 0) {
                send(datacenter, 0, evtTag, instanceGroups);
            }
        }
    }

    /**
     * Gets the event tag by the result of the inter-scheduler.
     * The result can be:
     * <ul>
     *     <li>{@link CloudSimTag#USER_REQUEST_SEND}: the target is datacenter and it can be forwarded again.</li>
     *     <li>{@link CloudSimTag#SCHEDULE_TO_DC_NO_FORWARD}: the target is datacenter and it cannot be forwarded again.</li>
     *     <li>{@link CloudSimTag#SCHEDULE_TO_DC_HOST}: the target is the host in the datacenter.</li>
     * </ul>
     * @param interSchedulerResult
     * @return
     */
    private int getEvtTagByInterSchedulerResult(InterSchedulerResult interSchedulerResult) {
        if (interSchedulerResult.getTarget() == InterSchedulerSimple.DC_TARGET) {
            if (interSchedulerResult.getIsSupportForward()) {
                return CloudSimTag.USER_REQUEST_SEND;
            } else {
                return CloudSimTag.SCHEDULE_TO_DC_NO_FORWARD;
            }
        } else if (interSchedulerResult.getTarget() == InterSchedulerSimple.HOST_TARGET) {
            return CloudSimTag.SCHEDULE_TO_DC_HOST;
        } else {
            throw new RuntimeException(String.format("%s: %s received an error target,it is not DC_TARGET or HOST_TARGET", getSimulation().clockStr(), getName()));
        }
    }


    /**
     * Handles the failed instance groups after the inter-scheduling.
     * Note that the user request that exceeds the time limit are not included.
     * @param collaborationId the collaboration id
     * @param failedInstanceGroups the failed instance groups
     */
    private void handleFailedInterScheduling(int collaborationId, List<InstanceGroup> failedInstanceGroups) {
        handleFailedInterScheduling(collaborationId, failedInstanceGroups, new HashSet<>());
    }

    /**
     * Handles the failed instance groups after the inter-scheduling.
     * @param collaborationId the collaboration id
     * @param failedInstanceGroups the failed instance groups
     * @param outDatedUserRequests the user requests that exceed the time limit
     */
    private void handleFailedInterScheduling(int collaborationId, List<InstanceGroup> failedInstanceGroups, Set<UserRequest> outDatedUserRequests) {
        List<InstanceGroup> retryInstanceGroups = new ArrayList<>();
        Set<UserRequest> failedUserRequests = outDatedUserRequests;
        for (UserRequest userRequest : outDatedUserRequests) {
            userRequest.addFailReason("outDated");
        }

        for (InstanceGroup instanceGroup : failedInstanceGroups) {
            //如果重试次数增加了之后没有超过最大重试次数，那么就将其重新放入队列中等待下次调度
            instanceGroup.addRetryNum();

            if (instanceGroup.isFailed()) {
                instanceGroup.getUserRequest().addFailReason("InstanceGroup" + instanceGroup.getId() + "Instance" + instanceGroup.getInstances().get(0).getId() + "expectedHostId:" + instanceGroup.getInstances().get(0).getExpectedScheduleHostId());

                failedUserRequests.add(instanceGroup.getUserRequest());
            } else {
                retryInstanceGroups.add(instanceGroup);
            }
        }

        if (!retryInstanceGroups.isEmpty()) {
            InterScheduler interScheduler = getSimulation().getCollaborationManager().getCollaborationCenterSchedulerMap().get(collaborationId);
            interScheduler.addInstanceGroups(retryInstanceGroups, true);
        }

        if (!failedUserRequests.isEmpty()) {
            send(this, 0, CloudSimTag.USER_REQUEST_FAIL, failedUserRequests);
            LOGGER.warn("{}: {}'s {} user requests failed.", getSimulation().clockStr(), getName(), failedUserRequests.size());
        }
    }

    /**
     * Allocates the bandwidth for the instance group and records the allocation information in database.
     * @param instanceGroup the instance group
     * @param receivedDatacenter the data center that the instance group is allocated to
     * @return true if the bandwidth is allocated successfully; false otherwise
     */
    private boolean allocateBwForGroup(InstanceGroup instanceGroup, Datacenter receivedDatacenter) {
        if (!tryAllocateBw(instanceGroup, receivedDatacenter)) {
            return false;
        }

        UserRequest userRequest = instanceGroup.getUserRequest();
        List<InstanceGroup> dstInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dst : dstInstanceGroups) {
            if (dst.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(instanceGroup, dst);
                if (!getSimulation().getNetworkTopology().allocateBw(receivedDatacenter, dst.getReceiveDatacenter(), edge.getRequiredBw())) {
                    return false;//After checking the tryAllocateBw function, there should be no failure to allocate bandwidth here.
                }

                getSimulation().getSqlRecord().recordInstanceGroupGraphAllocateInfo(receivedDatacenter.getId(), instanceGroup.getId(), dst.getReceiveDatacenter().getId(), dst.getId(), edge.getRequiredBw(), getSimulation().clock());
                userRequest.addAllocatedEdge(edge);
            }
        }
        List<InstanceGroup> srcInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup src : srcInstanceGroups) {
            if (src.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(src, instanceGroup);
                if (!getSimulation().getNetworkTopology().allocateBw(src.getReceiveDatacenter(), receivedDatacenter, edge.getRequiredBw())) {
                    return false;//After checking the tryAllocateBw function, there should be no failure to allocate bandwidth here.
                }

                getSimulation().getSqlRecord().recordInstanceGroupGraphAllocateInfo(src.getReceiveDatacenter().getId(), src.getId(), receivedDatacenter.getId(), instanceGroup.getId(), edge.getRequiredBw(), getSimulation().clock());
                userRequest.addAllocatedEdge(edge);
            }
        }
        return true;
    }

    private boolean tryAllocateBw(InstanceGroup instanceGroup, Datacenter receivedDatacenter) {
        Map<Datacenter, Map<Datacenter, Double>> allocatedBwTmp = new HashMap<>();

        List<InstanceGroup> dstInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dst : dstInstanceGroups) {
            if (dst.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(instanceGroup, dst);
                double nowBw;
                if (allocatedBwTmp.containsKey(receivedDatacenter) && allocatedBwTmp.get(receivedDatacenter).containsKey(dst.getReceiveDatacenter())) {
                    nowBw = allocatedBwTmp.get(receivedDatacenter).get(dst.getReceiveDatacenter());
                } else {
                    nowBw = getSimulation().getNetworkTopology().getBw(receivedDatacenter, dst.getReceiveDatacenter());
                }

                if (nowBw < edge.getRequiredBw()) {
                    return false;
                } else {
                    if (!allocatedBwTmp.containsKey(receivedDatacenter)) {
                        allocatedBwTmp.put(receivedDatacenter, new HashMap<>());
                    }
                    allocatedBwTmp.get(receivedDatacenter).put(dst.getReceiveDatacenter(), nowBw - edge.getRequiredBw());
                }
            }
        }

        List<InstanceGroup> srcInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup src : srcInstanceGroups) {
            if (src.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(src, instanceGroup);
                double nowBw;
                if (allocatedBwTmp.containsKey(src.getReceiveDatacenter()) && allocatedBwTmp.get(src.getReceiveDatacenter()).containsKey(receivedDatacenter)) {
                    nowBw = allocatedBwTmp.get(src.getReceiveDatacenter()).get(receivedDatacenter);
                } else {
                    nowBw = getSimulation().getNetworkTopology().getBw(src.getReceiveDatacenter(), receivedDatacenter);
                }

                if (nowBw < edge.getRequiredBw()) {
                    return false;
                } else {
                    if (!allocatedBwTmp.containsKey(src.getReceiveDatacenter())) {
                        allocatedBwTmp.put(src.getReceiveDatacenter(), new HashMap<>());
                    }
                    allocatedBwTmp.get(src.getReceiveDatacenter()).put(receivedDatacenter, nowBw - edge.getRequiredBw());
                }
            }
        }
        return true;
    }

    /**
     * Processes the event of changing the collaboration zone periodically.
     * @param evt the event
     */
    public void processChangeCollaborationSyn(SimEvent evt) {
        CollaborationManager collaborationManager = getSimulation().getCollaborationManager();
        collaborationManager.changeCollaboration();
        if (collaborationManager.getIsChangeCollaborationSyn()) {
            sendWithoutNetwork(this, collaborationManager.getChangeCollaborationSynTime(), CloudSimTag.CHANGE_COLLABORATION_SYN, null);
        }
    }

    /**
     * Processes the event of the user request failed.
     * All failed user requests will be sent here for processing.
     * It will record failed user requests into the database,
     * and then if there are instances in this user request that are already running on the host,
     * then it will end the running of these instances and release the occupied resources.
     * If an instance group has occupied the bandwidth between data centers, it will also be released.
     * The data of the event should be a set of user requests.
     * @param evt the event
     */
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

    /**
     * Processes a user request that has failed.
     * @param userRequest the user request
     */
    private void processAUserRequestFail(UserRequest userRequest) {
        if (userRequest.getState() == UserRequest.FAILED) {
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}: The UserRequest{} has failed. Reason: {}", getSimulation().clockStr(), userRequest.getId(), userRequest.getFailReason());
        }
        markUserRequestFailedAndRecord(userRequest);

        releaseBwForFailedUserRequest(userRequest);

        releaseHostResourceForFailedUserRequest(userRequest);
    }

    /**
     * Marks the user request as failed and records the finish information in the database.
     * @param userRequest the user request
     */
    private void markUserRequestFailedAndRecord(UserRequest userRequest) {
        userRequest.setState(UserRequest.FAILED);
        userRequest.setFinishTime(getSimulation().clock());
        getSimulation().getSqlRecord().recordUserRequestFinishInfo(userRequest);
    }

    /**
     * Releases the bandwidth occupied by the instance groups in the failed user request.
     * @param userRequest the failed user request
     */
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

    /**
     * Releases the host resources occupied by the instance in the failed user request.
     * @param userRequest the failed user request
     */
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

    /**
     * Marks the instance group as failed and records the finish information in the database.
     * @param instanceGroup the instance group in the failed user request
     */
    private void markInstanceGroupFailedAndRecord(InstanceGroup instanceGroup) {
        instanceGroup.setFinishTime(getSimulation().clock());

        if (instanceGroup.getState() == UserRequest.SCHEDULING) {
            instanceGroup.setState(UserRequest.FAILED);
            getSimulation().getSqlRecord().recordInstanceGroupFinishInfo(instanceGroup);
        } else {
            instanceGroup.setState(UserRequest.FAILED);
            getSimulation().getSqlRecord().recordInstanceGroupAllInfo(instanceGroup);
        }
    }

    /**
     * Distinguish all instances in the instance group.
     * If they are running, add them to the endInstances dictionary.
     * If they are not running, update the relevant information and add them to the recordInstances dictionary.
     * @param recordInstances the list of instances to be recorded
     * @param endInstances the map of instances to be ended
     * @param instanceGroup the instance group
     */
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