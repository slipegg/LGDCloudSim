package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.SynState;

/**
 * The intra-scheduler that extends the {@link IntraSchedulerLeastRequested} class.
 * It has changed the score calculation method.
 * The more recently synchronized a partition is, the higher the score.
 * The score is calculated as follows:
 * score = random.nextDouble(100)-100*(synPartitionId - partitionId + partitionNum) % partitionNum
 *
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerRandomScoreByPartitionSynOrder extends IntraSchedulerLeastRequested {
    /**
     * The latest synchronization partition id.
     */
    private int synPartitionId;

    /**
     * Construct the intra-scheduler with the id, the first partition id and the partition number.
     *
     * @param id               the intra-scheduler id.
     * @param firstPartitionId the first synchronization partition id.
     * @param partitionNum     the number of partitions in the data center.
     */
    public IntraSchedulerRandomScoreByPartitionSynOrder(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    /**
     * Get the latest synchronization partition id before scheduling.
     * Because it will be used frequently.
     */
    @Override
    protected void processBeforeSchedule(){
        excludeTimeNanos = 0;
        scoreHostHistoryMap.clear();
        synPartitionId = firstPartitionId;
        if (datacenter.getStatesManager().isSynCostTime()) {
            synPartitionId = (firstPartitionId + datacenter.getStatesManager().getPartitionSynCount()) % partitionNum;
        }
    }

    /**
     * Get the score for the host.
     * The more recently synchronized a partition is, the higher the score.
     *
     * @param instance the instance to be scheduled.
     * @param hostId the id of the host.
     * @param synState the synchronization state.
     * @return the score for the host.
     */
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
