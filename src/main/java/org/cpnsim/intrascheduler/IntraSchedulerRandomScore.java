package org.cpnsim.intrascheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.SynState;

public class IntraSchedulerRandomScore extends IntraSchedulerLeastRequested {
    public IntraSchedulerRandomScore(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    protected double getScoreForHost(Instance instance, int hostId, SynState synState){
        long startTime = System.nanoTime();
        HostState hostState = synState.getHostState(hostId);
        long endTime  = System.nanoTime();
        excludeTimeNanos += endTime - startTime;
        if (!hostState.isSuitable(instance)) {
            return -1;
        } else {
            if(scoreHostHistoryMap.containsKey(hostId)){
                return scoreHostHistoryMap.get(hostId);
            }else{
                double score = random.nextDouble(100);
                scoreHostHistoryMap.put(hostId, score);
                return score;
            }
        }
    }
}
