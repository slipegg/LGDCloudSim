package org.scalecloudsim.statemanager;

import lombok.Getter;
import org.scalecloudsim.request.Instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SynStateSimple implements SynState {
    @Getter
    //partitionId, hostId, hostState
    Map<Integer, Map<Integer, int[]>> synState;
    int[] nowHostStates;
    PartitionRangesManager partitionRangesManager;
    @Getter
    Map<Integer, Map<Integer, int[]>> selfHostState;

    public SynStateSimple(Map<Integer, Map<Integer, int[]>> synState, int[] nowHostStates, PartitionRangesManager partitionRangesManager, Map<Integer, Map<Integer, int[]>> selfHostState) {
        this.synState = synState;
        this.nowHostStates = nowHostStates;
        this.partitionRangesManager = partitionRangesManager;
        this.selfHostState = selfHostState;
    }

    @Override
    public boolean isSuitable(int hostId, Instance instance) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        //在自己维护的状态表中
        if (selfHostState.get(partitionId).containsKey(hostId)) {
            int[] hostState = selfHostState.get(partitionId).get(hostId);
            return hostState[0] >= instance.getCpu() && hostState[1] >= instance.getRam() && hostState[2] >= instance.getStorage() && hostState[3] >= instance.getBw();
        } else if (synState.get(partitionId).containsKey(hostId)) {
            int[] hostState = synState.get(partitionId).get(hostId);
            return hostState[0] >= instance.getCpu() && hostState[1] >= instance.getRam() && hostState[2] >= instance.getStorage() && hostState[3] >= instance.getBw();
        } else {
            return nowHostStates[hostId * HostState.STATE_NUM] >= instance.getCpu() && nowHostStates[hostId * HostState.STATE_NUM + 1] >= instance.getRam() && nowHostStates[hostId * HostState.STATE_NUM + 2] >= instance.getStorage() && nowHostStates[hostId * HostState.STATE_NUM + 3] >= instance.getBw();
        }
    }

    @Override
    public void allocateTmpResource(int hostId, Instance instance) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        if (selfHostState.get(partitionId).containsKey(hostId)) {
            int[] hostState = selfHostState.get(partitionId).get(hostId);
            hostState[0] -= instance.getCpu();
            hostState[1] -= instance.getRam();
            hostState[2] -= instance.getStorage();
            hostState[3] -= instance.getBw();
        } else if (synState.get(partitionId).containsKey(hostId)) {
            int[] hostState = synState.get(partitionId).get(hostId);
            selfHostState.get(partitionId).put(hostId, new int[]{
                    hostState[0] - instance.getCpu(),
                    hostState[1] - instance.getRam(),
                    hostState[2] - instance.getStorage(),
                    hostState[3] - instance.getBw()
            });
        } else {
            selfHostState.get(partitionId).put(hostId, new int[]{
                    nowHostStates[hostId * HostState.STATE_NUM] - instance.getCpu(),
                    nowHostStates[hostId * HostState.STATE_NUM + 1] - instance.getRam(),
                    nowHostStates[hostId * HostState.STATE_NUM + 2] - instance.getStorage(),
                    nowHostStates[hostId * HostState.STATE_NUM + 3] - instance.getBw()
            });
        }
    }
}
