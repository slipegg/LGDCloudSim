package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.innerscheduler.InnerScheduleResult;
import org.cpnsim.request.Instance;
import org.cpnsim.request.UserRequest;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.StatesManager;

import java.util.*;

public class ResourceAllocateSelectorSimple implements ResourceAllocateSelector {
    @Getter
    @Setter
    Datacenter datacenter;
    @Getter
    Map<Integer, Integer> partitionConflicts = new HashMap<>();

    @Override
    public Map<Integer, List<Instance>> selectResourceAllocate(List<InnerScheduleResult> innerScheduleResults) {
        Map<Integer, List<Instance>> res = new HashMap<>();
        StatesManager statesManager = datacenter.getStatesManager();
        for (InnerScheduleResult innerScheduleResult : innerScheduleResults) {
            Map<Integer, List<Instance>> scheduleRes = innerScheduleResult.getScheduleResult();
            for (Map.Entry<Integer, List<Instance>> entry : scheduleRes.entrySet()) {
                int hostId = entry.getKey();
                List<Instance> instances = entry.getValue();
                HostState hostState = statesManager.getNowHostState(hostId);//注意这里是拷贝，不影响原始的状态
                for (Instance instance : instances) {
                    if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                        continue;
                    }
                    if (hostState.isSuitable(instance)) {
                        hostState.allocate(instance);
                        res.putIfAbsent(hostId, new ArrayList<>());
                        res.get(hostId).add(instance);
                    } else {
                        res.putIfAbsent(-1, new ArrayList<>());
                        res.get(-1).add(instance);
                        int partitionId = datacenter.getStatesManager().getPartitionRangesManager().getPartitionId(hostId);
                        if (partitionConflicts.containsKey(partitionId)) {
                            partitionConflicts.put(partitionId, partitionConflicts.get(partitionId) + 1);
                        } else {
                            partitionConflicts.put(partitionId, 1);
                        }
                    }
                }
            }
        }
        return res;
    }
}
