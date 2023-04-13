package org.scalecloudsim.statemanager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PredictionManagerSimple implements PredictionManager {
    @Override
    public Map<Integer, HostStateHistory> predictHostStates(PartitionManager partitionManager, double delay) {
        List<Integer> hosts = partitionManager.getDelayPartitionState(delay).keySet().stream().toList();
        Map<Integer, HostStateHistory> predictResult = new HashMap<>();
        for (Integer hostId : hosts) {
            LinkedList<HostStateHistory> hostHistory = partitionManager.getHostHistory(hostId, delay);
            if (hostHistory.size() > 0) {
                HostStateHistory predictHostState = predictHostState(hostHistory);
                predictResult.put(hostId, predictHostState);
            }
        }
        return predictResult;
    }

    //这里的预测方法是直接取过去的平均值
    private HostStateHistory predictHostState(LinkedList<HostStateHistory> hostHistory) {
        int cpu = (int) hostHistory.stream().mapToLong(HostStateHistory::getCpu).average().getAsDouble();
        int ram = (int) hostHistory.stream().mapToLong(HostStateHistory::getRam).average().getAsDouble();
        int storage = (int) hostHistory.stream().mapToLong(HostStateHistory::getStorage).average().getAsDouble();
        int bw = (int) hostHistory.stream().mapToLong(HostStateHistory::getBw).average().getAsDouble();
        return new HostStateHistory(cpu, ram, storage, bw, -2);
    }
}
