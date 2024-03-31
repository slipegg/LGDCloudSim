package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.SynState;

/**
 * The intra-scheduler that extends the {@link IntraSchedulerLeastRequested} class.
 * It has changed the score calculation method to random score.
 * So that the intra-scheduler is like schedule the instances to the suitable host randomly.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerRandomScore extends IntraSchedulerLeastRequested {
    /**
     * Construct the intra-scheduler with the id, the first partition id and the partition number.
     *
     * @param id               the intra-scheduler id.
     * @param firstPartitionId the first synchronization partition id.
     * @param partitionNum     the number of partitions in the data center.
     */
    public IntraSchedulerRandomScore(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    /**
     * The score is calculated by the random score.
     * Note that the score also will be cached in the scoreHostHistoryMap.
     * @param instance the instance to be scheduled.
     * @param hostId the id of the host.
     * @param synState the synchronization state.
     * @return the score for the host.
     */
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
