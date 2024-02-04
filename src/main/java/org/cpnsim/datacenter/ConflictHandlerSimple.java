package org.cpnsim.datacenter;

import lombok.Getter;
import org.cpnsim.intrascheduler.IntraScheduler;
import org.cpnsim.intrascheduler.IntraSchedulerResult;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.StatesManager;

import java.util.*;

/**
 * A class to represent a resource allocate selector with simple strategy.
 * It will check in the order of the results to see if there are still resources available to allocate the instance
 * This class implements the interface {@link ConflictHandler}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
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
        StatesManager statesManager = datacenter.getStatesManager();
        int conflictSum = 0;
        double nowTime = datacenter.getSimulation().clock();

        for (IntraSchedulerResult intraSchedulerResult : intraSchedulerResults) {
            List<Instance> successRes = new ArrayList<>();
            List<Instance> failRes = new ArrayList<>();
            Set<UserRequest> outdatedRequests = new HashSet<>();
            for (Instance instance : intraSchedulerResult.getScheduledInstances()) {
                if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                    continue;
                }

                if (instance.getUserRequest().getScheduleDelayLimit() > 0 && nowTime - instance.getUserRequest().getSubmitTime() > instance.getUserRequest().getScheduleDelayLimit()) {
                    outdatedRequests.add(instance.getUserRequest());
                    continue;
                }

                int hostId = instance.getExpectedScheduleHostId();
                HostState hostState;
                if (allocateHostStates.containsKey(hostId)) {
                    hostState = allocateHostStates.get(hostId);
                } else {
                    hostState = statesManager.getNowHostState(hostId);
                    allocateHostStates.put(hostId, hostState);
                }

                if (hostState.isSuitable(instance)) {
                    hostState.allocate(instance);
                    successRes.add(instance);
                } else {
                    failRes.add(instance);

                    int partitionId = datacenter.getStatesManager().getPartitionRangesManager().getPartitionId(hostId);
                    if (partitionConflicts.containsKey(partitionId)) {
                        partitionConflicts.put(partitionId, partitionConflicts.get(partitionId) + 1);
                    } else {
                        partitionConflicts.put(partitionId, 1);
                    }
                    conflictSum += 1;
                }
            }
            conflictHandlerResult.addSuccessRes(intraSchedulerResult.getIntraScheduler(), successRes);
            conflictHandlerResult.addFailRes(intraSchedulerResult.getIntraScheduler(), failRes, outdatedRequests);
        }
        if (conflictSum != 0) {
            getDatacenter().getSimulation().getSqlRecord().recordConflict(getDatacenter().getSimulation().clock(), conflictSum);
        }
        return conflictHandlerResult;
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
                    hostState = getDatacenter().getStatesManager().getNowHostState(instance.getExpectedScheduleHostId());
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
