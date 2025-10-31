package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.SynState;
import org.lgdcloudsim.util.utils;

import java.util.*;

/**
 * The random intra-scheduler that extends the {@link IntraSchedulerSimple} class.
 * It will filter the suitable host from the first host id to the last host id.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerRandomGang extends IntraSchedulerSimple {
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
    public IntraSchedulerRandomGang(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    private void randomScheduleForInstanceGroup(InstanceGroup instanceGroup, SynState synState, IntraSchedulerResult intraSchedulerResult ) {
        List<Instance> instances = instanceGroup.getInstances();
        List<Integer> innerSchedulerView = datacenter.getStatesManager().getIntraSchedulerView(this);
        int hostNum = innerSchedulerView.get(1)-innerSchedulerView.get(0)+1;

        Map<Integer, HostState> scheduledHostStateMap = new HashMap<>();
        List<Integer> candidateHostIds = new ArrayList<>();
        for (Instance instance : instances) {
            int startHostId = random.nextInt(hostNum);
            int i = 0;
            for (; i < hostNum; i++) {
                int hostId = (startHostId + i) % hostNum+innerSchedulerView.get(0);
                HostState hostState = scheduledHostStateMap.getOrDefault(hostId, synState.getHostState(hostId));
                if (hostState.isSuitable(instance)) {
                    hostState.allocate(instance);
                    candidateHostIds.add(hostId);
                    scheduledHostStateMap.put(hostId, hostState);
                    break;
                }
            }

            if (i == hostNum) {
                break;
            }
        }

        if (candidateHostIds.size() == instances.size()) {
            for (int i = 0; i < instances.size(); i++) {
                Instance instance = instances.get(i);
                int hostId = candidateHostIds.get(i);
                synState.allocateTmpResource(hostId, instance);
                instance.setExpectedScheduleHostId(hostId);
                intraSchedulerResult.addScheduledInstance(instance);
            }
        } else {
            for (Instance instance : instances) {
                intraSchedulerResult.addFailedScheduledInstance(instance);
            }
        }
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

        List<InstanceGroup> instanceGroups = utils.groupToInstanceGroups(instances);

        for (InstanceGroup instanceGroup : instanceGroups) {
            randomScheduleForInstanceGroup(instanceGroup, synState, intraSchedulerResult);
        }

        return intraSchedulerResult;
    }
}
