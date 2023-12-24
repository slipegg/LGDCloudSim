package org.cpnsim.innerscheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.SynState;
import org.cpnsim.util.ScoredHostsManager;

import java.util.HashMap;
import java.util.Map;

public class InnerSchedulerRandomScoreByPartitionSynOrder extends InnerSchedulerLeastRequested {
    private int synPartitionId;

    public InnerSchedulerRandomScoreByPartitionSynOrder(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

//    @Override
//    protected ScoredHostsManager getScoredHostsManager(Instance instance, int randomStartIndex, int scoredHostNum, SynState synState) {
//        ScoredHostsManager scoredHostsManager = new ScoredHostsManager(new HashMap<>(Map.of(datacenter, scoreHostHistoryMap)));
//
//        for (int p = 0; p < partitionNum; p++) {
//            int[] range = datacenter.getStatesManager().getPartitionRangesManager().getRange((synPartitionId + partitionNum - p) % partitionNum);
//            int startHostId = random.nextInt(range[1] - range[0] + 1);
//            int rangeLength = range[1] - range[0] + 1;
//            for (int i = 0; i < rangeLength; i++) {
//                int hostId = (startHostId + i) % rangeLength + range[0];
//                double score = getScoreForHost(instance, hostId, synState);
//                if (score == -1) {
//                    continue;
//                }
//
//                scoredHostsManager.addScoredHost(hostId, datacenter, score);
//
//                if (scoredHostsManager.getScoredHostNum() >= scoredHostNum) {
//                    break;
//                }
//            }
//            if (scoredHostsManager.getScoredHostNum() >= scoredHostNum) {
//                break;
//            }
//        }
//
//        return scoredHostsManager;
//    }

    @Override
    protected void processBeforeSchedule(){
        excludeTimeNanos = 0;
        scoreHostHistoryMap.clear();
        synPartitionId = firstPartitionId;
        if (datacenter.getStatesManager().isSynCostTime()) {
            synPartitionId = (firstPartitionId + datacenter.getStatesManager().getSmallSynGapCount()) % partitionNum;
        }
    }

    @Override
    protected double getScoreForHost(Instance instance, int hostId, SynState synState) {
        long startTime = System.nanoTime();
        HostState hostState = synState.getHostState(hostId);
        long endTime  = System.nanoTime();
        excludeTimeNanos += endTime - startTime;
        if (!hostState.isSuitable(instance)) {
            return -1;
        } else {
            if (scoreHostHistoryMap.containsKey(hostId)) {
                return scoreHostHistoryMap.get(hostId);
            } else {
                int partitionId = datacenter.getStatesManager().getPartitionRangesManager().getPartitionId(hostId);
                double score = random.nextDouble(100)-100*(synPartitionId - partitionId + partitionNum) % partitionNum;
                scoreHostHistoryMap.put(hostId, score);
                return score;
            }
        }
    }
}
