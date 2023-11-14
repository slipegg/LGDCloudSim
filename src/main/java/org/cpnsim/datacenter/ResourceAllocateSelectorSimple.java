package org.cpnsim.datacenter;

import lombok.Getter;
import org.cpnsim.innerscheduler.InnerScheduleResult;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.StatesManager;

import java.util.*;

/**
 * A class to represent a resource allocate selector with simple strategy.
 * It will check in the order of the results to see if there are still resources available to allocate the instance
 * This class implements the interface {@link ResourceAllocateSelector}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class ResourceAllocateSelectorSimple implements ResourceAllocateSelector {
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
    public ResourceAllocateResult selectResourceAllocate(List<InnerScheduleResult> innerScheduleResults) {
        Map<Integer, List<Instance>> successRes = new HashMap<>();
        Map<InnerScheduler, List<Instance>> failRes = null;
        Map<Integer, HostState> allocateHostStates = new HashMap<>();
        StatesManager statesManager = datacenter.getStatesManager();
        int conflictSum = 0;
        for (InnerScheduleResult innerScheduleResult : innerScheduleResults) {
            Map<Integer, List<Instance>> scheduleRes = innerScheduleResult.getScheduleResult();
            for (Map.Entry<Integer, List<Instance>> entry : scheduleRes.entrySet()) {
                int hostId = entry.getKey();
                List<Instance> instances = entry.getValue();
                HostState hostState;
                if (allocateHostStates.containsKey(hostId)) {
                    hostState = allocateHostStates.get(hostId);
                } else {
                    hostState = statesManager.getNowHostState(hostId);
                    allocateHostStates.put(hostId, hostState);
                }
                for (Instance instance : instances) {
                    if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                        continue;
                    }
                    if (hostState.isSuitable(instance)) {
                        hostState.allocate(instance);
                        successRes.putIfAbsent(hostId, new ArrayList<>());
                        successRes.get(hostId).add(instance);
                    } else {
                        instance.addRetryHostId(hostId);
                        if (failRes == null) {
                            failRes = new HashMap<>();
                        }
                        failRes.putIfAbsent(innerScheduleResult.getInnerScheduler(), new ArrayList<>());
                        failRes.get(innerScheduleResult.getInnerScheduler()).add(instance);

                        int partitionId = datacenter.getStatesManager().getPartitionRangesManager().getPartitionId(hostId);
                        if (partitionConflicts.containsKey(partitionId)) {
                            partitionConflicts.put(partitionId, partitionConflicts.get(partitionId) + 1);
                        } else {
                            partitionConflicts.put(partitionId, 1);
                        }
                        conflictSum += 1;
                    }
                }
            }
        }
        if (conflictSum != 0) {
            getDatacenter().getSimulation().getSqlRecord().recordConflict(getDatacenter().getSimulation().clock(), conflictSum);
        }
        return new ResourceAllocateResult(successRes, failRes);
    }

    @Override
    public List<InstanceGroup> filterConflictedInstanceGroup(List<InstanceGroup> instanceGroups) {
        List<InstanceGroup> failedScheduledInstanceGroups = new ArrayList<>();

        for (InstanceGroup instanceGroup : instanceGroups) {
            Map<Integer, HostState> hostStatesIfScheduled = new HashMap<>();
            for (Instance instance : instanceGroup.getInstances()) {
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

        return failedScheduledInstanceGroups;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
    }
}
