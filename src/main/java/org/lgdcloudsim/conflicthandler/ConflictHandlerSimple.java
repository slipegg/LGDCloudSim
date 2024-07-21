package org.lgdcloudsim.conflicthandler;

import lombok.Getter;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.intrascheduler.IntraSchedulerResult;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.StatesManager;
import org.lgdcloudsim.util.FailedOutdatedResult;

import java.util.*;

/**
 * A class to represent a resource allocate selector with simple strategy.
 * It will check in the order of the results to see if there are still resources available to allocate the instance
 * This class implements the interface {@link ConflictHandler}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class ConflictHandlerSimple implements ConflictHandler {
    /**
     * the datacenter.
     **/
    @Getter
    Datacenter datacenter;

    /**
     * the number of conflicts when resource allocating.
     **/
    @Getter
    Map<Integer, Integer> partitionConflicts = new HashMap<>();

    @Override
    public ConflictHandlerResult filterConflictedInstance(List<IntraSchedulerResult> intraSchedulerResults) {
        ConflictHandlerResult conflictHandlerResult = new ConflictHandlerResult();
        Map<Integer, HostState> allocateHostStates = new HashMap<>();
        int conflictSum = 0;

        for (IntraSchedulerResult intraSchedulerResult : intraSchedulerResults) {
            List<Instance> successRes = new ArrayList<>();
            List<Instance> failRes = new ArrayList<>();
            Set<UserRequest> outdatedRequests = new HashSet<>();
            conflictSum += dealConflictInstance(intraSchedulerResult.getScheduledInstances(), successRes, failRes, outdatedRequests, allocateHostStates, allocateHostStates);
            conflictHandlerResult.addSuccessRes(intraSchedulerResult.getIntraScheduler(), successRes);
            conflictHandlerResult.addFailRes(intraSchedulerResult.getIntraScheduler(), failRes, outdatedRequests);
        }
        if (conflictSum != 0) {
            getDatacenter().getSimulation().getSqlRecord().recordConflict(getDatacenter().getSimulation().clock(), conflictSum);
        }
        return conflictHandlerResult;
    }

    /**
     * Deal with the conflict instances.
     *
     * @param scheduledInstances    the instances to be scheduled.
     * @param successInstances      the instances allocated to the host successfully.
     * @param failedInstances       the instances need to be rescheduled or be marked as failed.
     * @param outdatedRequests      the outdated requests.
     * @param hostStatesIfScheduled the host states if the instances are scheduled.
     * @param allocateHostStates    the host states if the instances are allocated.
     * @return the number of conflicts when resource allocating.
     */
    private int dealConflictInstance(List<Instance> scheduledInstances, List<Instance> successInstances, List<Instance> failedInstances, Set<UserRequest> outdatedRequests, Map<Integer, HostState> hostStatesIfScheduled, Map<Integer, HostState> allocateHostStates) {
        StatesManager statesManager = datacenter.getStatesManager();
        int conflictSum = 0;
        for (Instance instance : scheduledInstances) {
            if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                continue;
            }

            if (instance.getUserRequest().getScheduleDelayLimit() > 0 && getDatacenter().getSimulation().clock() - instance.getUserRequest().getSubmitTime() > instance.getUserRequest().getScheduleDelayLimit()) {
                outdatedRequests.add(instance.getUserRequest());
                continue;
            }


            int hostId = instance.getExpectedScheduleHostId();
            HostState hostState;
            if (allocateHostStates.containsKey(hostId)) {
                hostState = allocateHostStates.get(hostId);
            } else {
                hostState = statesManager.getCenterHostState(hostId);
                allocateHostStates.put(hostId, hostState);
            }

            if (hostState.isSuitable(instance)) {
                hostState.allocate(instance);
                successInstances.add(instance);
            } else {
                failedInstances.add(instance);

                int partitionId = datacenter.getStatesManager().getPartitionRangesManager().getPartitionId(hostId);
                if (partitionConflicts.containsKey(partitionId)) {
                    partitionConflicts.put(partitionId, partitionConflicts.get(partitionId) + 1);
                } else {
                    partitionConflicts.put(partitionId, 1);
                }
                conflictSum += 1;
            }
        }

        return conflictSum;
    }

    @Override
    public FailedOutdatedResult<InstanceGroup> filterConflictedInstanceGroup(List<InstanceGroup> instanceGroups) {
        List<InstanceGroup> failedScheduledInstanceGroups = new ArrayList<>();
        Set<UserRequest> outdatedRequests = new HashSet<>();
        double nowTime = getDatacenter().getSimulation().clock();

        Map<Integer, HostState> hostStatesIfScheduled = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            for (Instance instance : instanceGroup.getInstances()) {
                if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                    continue;
                }

                if (instance.getUserRequest().getScheduleDelayLimit() > 0 && nowTime - instance.getUserRequest().getSubmitTime() > instance.getUserRequest().getScheduleDelayLimit()) {
                    outdatedRequests.add(instance.getUserRequest());
                    continue;
                }

                HostState hostState;
                if (hostStatesIfScheduled.containsKey(instance.getExpectedScheduleHostId())) {
                    hostState = hostStatesIfScheduled.get(instance.getExpectedScheduleHostId());
                } else {
                    hostState = getDatacenter().getStatesManager().getActualHostState(instance.getExpectedScheduleHostId());
                }

                if (hostState.isSuitable(instance)) {
                    hostState.allocate(instance);
                    hostStatesIfScheduled.put(instance.getExpectedScheduleHostId(), hostState);
                } else {
                    failedScheduledInstanceGroups.add(instanceGroup);
                    break;
                }
            }
        }

        return new FailedOutdatedResult<InstanceGroup>(failedScheduledInstanceGroups, outdatedRequests);
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
    }
}
