package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.statemanager.SynState;

import java.util.*;

/**
 * The random intra-scheduler that extends the {@link IntraSchedulerSimple} class.
 * It will filter the suitable host from the first host id to the last host id.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerRandom extends IntraSchedulerSimple {
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
    public IntraSchedulerRandom(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    /**
     * Schedule the instances from the first host id to the last host id.
     * @param instances the instances to be scheduled.
     * @param synState the synchronization state.
     * @return the intra-scheduler result.
     */
    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());

        List<Integer> innerSchedulerView = datacenter.getStatesManager().getIntraSchedulerView(this);
        int hostNum = innerSchedulerView.get(1)-innerSchedulerView.get(0)+1;

        for (Instance instance : instances) {
            int suitId = -1;

            int startHostId = random.nextInt(hostNum);
            for (int i = 0; i < hostNum; i++) {
                int hostId = (startHostId + i) % hostNum+innerSchedulerView.get(0);
                if (synState.isSuitable(hostId, instance)) {
                    suitId = hostId;
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
