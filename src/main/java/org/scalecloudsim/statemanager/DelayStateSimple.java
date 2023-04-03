package org.scalecloudsim.statemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//一个大表加一堆小表用来表示状态
public class DelayStateSimple implements DelayState {
    int[] nowHostState;
    Map<Integer, Map<Integer, HostStateHistory>> oldState;//partitionId, hostId, hostState
    PartitionRangesManager partitionRangesManager;

    public DelayStateSimple(int[] nowHostState, Map<Integer, Map<Integer, HostStateHistory>> oldState, PartitionRangesManager partitionRangesManager) {
        this.nowHostState = nowHostState;
        this.oldState = oldState;
        this.partitionRangesManager = partitionRangesManager;
    }

    @Override
    public int[] getHostState(int hostId) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        if (oldState.get(partitionId).containsKey(hostId)) {
            return oldState.get(partitionId).get(hostId).getStateArray();
        } else {
            return Arrays.copyOfRange(nowHostState, hostId * HostState.STATE_NUM, (hostId + 1) * HostState.STATE_NUM);
        }
    }

    //这里会创建新数组，比较消耗内存，谨慎使用
    @Override
    public int[] getAllState() {
        int[] allState = new int[nowHostState.length];
        System.arraycopy(nowHostState, 0, allState, 0, nowHostState.length);
        for (int partitionId : oldState.keySet()) {
            for (int hostId : oldState.get(partitionId).keySet()) {

                int[] hostState = oldState.get(partitionId).get(hostId).getStateArray();
                System.arraycopy(hostState, 0, allState, hostId * HostState.STATE_NUM, HostState.STATE_NUM);
            }
        }
        return allState;
    }
}
