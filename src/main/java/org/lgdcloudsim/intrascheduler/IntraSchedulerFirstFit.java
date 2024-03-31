package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.statemanager.SynState;

import java.util.List;

/**
 * The intra-scheduler that extends the {@link IntraSchedulerSimple} class
 * It will filter the suitable host from the last scheduled host id and the last scheduled partition id.
 * It will try to find the suitable host from the last scheduled partition id.
 * When traversing the hosts in the partition, it will start from the last scheduled host id.
 * If there is no suitable host in the partition, it will try to find the suitable host from the next partition.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerFirstFit extends IntraSchedulerSimple {
    /**
     * The last scheduled host index.
     */
    int lastHostIndex = 0;

    /**
     * The last scheduled partition index.
     */
    int lastPartitionIndex = 0;

    /**
     * Construct the intra-scheduler with the id, the first partition id and the partition number.
     *
     * @param id               the intra-scheduler id.
     * @param firstPartitionId the first synchronization partition id.
     * @param partitionNum     the number of partitions in the data center.
     */
    public IntraSchedulerFirstFit(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    /**
     * Schedule the instances from the last scheduled host id and the last scheduled partition id.
     * @param instances the instances to be scheduled.
     * @param synState the synchronization state.
     * @return the intra-scheduler result.
     */
    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());
        for (Instance instance : instances) {
            int suitId = -1;

            int p = 0;
            for (; p < partitionNum; p++) {
                int[] range = datacenter.getStatesManager().getPartitionRangesManager().getRange(lastPartitionIndex);
                for (int i = range[0]; i <= range[1]; i++) {
                    if (synState.isSuitable(lastHostIndex, instance)) {
                        suitId = lastHostIndex;
                        break;
                    }
                    lastHostIndex = ++lastHostIndex % (range[1] - range[0] + 1);
                }
                if (suitId != -1) {
                    break;
                }
                lastPartitionIndex = ++lastPartitionIndex % partitionNum;
            }
            if (suitId != -1) {
                synState.allocateTmpResource(suitId, instance);
                instance.setExpectedScheduleHostId(suitId);
                intraSchedulerResult.addScheduledInstance(instance);
            } else {
                intraSchedulerResult.addFailedScheduledInstance(instance);
            }
        }

        return intraSchedulerResult;
    }
}
