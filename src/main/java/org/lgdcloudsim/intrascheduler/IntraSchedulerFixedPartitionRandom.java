package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.statemanager.SynState;

import java.util.*;

/**
 * The intra-scheduler that extends the {@link IntraSchedulerSimple} class
 * It will filter the suitable host from a fixed partition id which is related to the intra-scheduler id.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerFixedPartitionRandom extends IntraSchedulerSimple {
    /**
     * The random object.
     */
    Random random = new Random();

    /**
     * Construct the intra-scheduler with the id, the first partition id and the partition number.
     *
     * @param id               the intra-scheduler id.
     * @param firstPartitionId the first synchronization partition id.
     * @param partitionNum     the number of partitions in the data center.
     */
    public IntraSchedulerFixedPartitionRandom(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    /**
     * Schedule the instances from a fixed partition id which is related to the intra-scheduler id.
     * @param instances the instances to be scheduled.
     * @param synState the synchronization state.
     * @return the intra-scheduler result.
     */
    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());
        int partitionNum = datacenter.getIntraSchedulers().size();
        int hostSum = datacenter.getStatesManager().getHostNum();
        int firstPartitionId = this.id;
        for (Instance instance : instances) {
            int suitId = -1;
            for (int p = 0; p < partitionNum; p++) {
                int partId = (firstPartitionId + partitionNum - p) % partitionNum;
                int startId = partId * (hostSum / partitionNum);
                int rangeLength;
                if (partId == partitionNum - 1) {
                    rangeLength = hostSum - startId;
                } else {
                    rangeLength = (partId + 1) * (hostSum / partitionNum) - startId;
                }
                int selectId = random.nextInt(rangeLength);
                for (int i = 0; i < rangeLength; i++) {
                    int hostId = startId + (selectId + i) % rangeLength;
                    if (synState.isSuitable(hostId, instance)) {
                        suitId = hostId;
                        break;
                    }
                }
                if (suitId != -1) {
                    break;
                }
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
