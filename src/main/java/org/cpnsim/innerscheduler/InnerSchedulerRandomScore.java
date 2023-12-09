package org.cpnsim.innerscheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.SynState;

public class InnerSchedulerRandomScore extends InnerSchedulerLeastRequested{
    public InnerSchedulerRandomScore(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    protected double getScoreForHost(Instance instance, int hostId, SynState synState){
        HostState hostState = synState.getHostState(hostId);
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
