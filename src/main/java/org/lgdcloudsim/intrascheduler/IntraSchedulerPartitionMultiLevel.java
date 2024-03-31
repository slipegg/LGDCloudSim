package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.statemanager.PartitionRangesManager;
import org.lgdcloudsim.statemanager.SynState;

import java.util.*;

/**
 * The intra-scheduler that extends the {@link IntraSchedulerSimple} class.
 * It is designed to avoid scheduling conflict.
 * It's scheduling strategy is as follows:
 * <ul>
 *     <li>Get the latest synchronization partition id from the {@link org.lgdcloudsim.statemanager.StatesManager}.</li>
 *     <li>Divide the partition to be scheduled into groups which are equal to the number of partitions in the data center.</li>
 *     <li>The hosts in each group have their own preference for scheduling,
 *     which is related to the ID of the partition that the scheduler first synchronizes.
 *     When scheduling within a partition, we first start from a random group and check
 *     whether the host of our own preference in each group matches.
 *     If not, check the host of the next preference in the group.</li>
 *     <li>If this partition does not exist, then check the next most recently synchronized host
 *     and repeat the above steps until a suitable host is found.</li>
 * </ul>
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class IntraSchedulerPartitionMultiLevel extends IntraSchedulerSimple {
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
    public IntraSchedulerPartitionMultiLevel(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    /**
     * Schedule the instances.
     * It's scheduling strategy is as follows:
     * <ul>
     *     <li>Get the latest synchronization partition id from the {@link org.lgdcloudsim.statemanager.StatesManager}.</li>
     *     <li>Divide the partition to be scheduled into groups which are equal to the number of partitions in the data center.</li>
     *     <li>The hosts in each group have their own preference for scheduling,
     *     which is related to the ID of the partition that the scheduler first synchronizes.
     *     When scheduling within a partition, we first start from a random group and check
     *     whether the host of our own preference in each group matches.
     *     If not, check the host of the next preference in the group.</li>
     *     <li>If this partition does not exist, then check the next most recently synchronized host
     *     and repeat the above steps until a suitable host is found.</li>
     * </ul>
     *
     * @param instances the instances to be scheduled.
     * @param synState the synchronization state.
     * @return the intra-scheduler result.
     */
    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());

        for (Instance instance : instances) {
            int suitId = getSuitHostId(synState, instance);

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

    /**
     * Get the suitable host id for the instance.
     * It's scheduling strategy is as follows:
     * <ul>
     *     <li>Get the latest synchronization partition id from the {@link org.lgdcloudsim.statemanager.StatesManager}.</li>
     *     <li>Divide the partition to be scheduled into groups which are equal to the number of partitions in the data center.</li>
     *     <li>The hosts in each group have their own preference for scheduling,
     *     which is related to the ID of the partition that the scheduler first synchronizes.
     *     When scheduling within a partition, we first start from a random group and check
     *     whether the host of our own preference in each group matches.
     *     If not, check the host of the next preference in the group.</li>
     *     <li>If this partition does not exist, then check the next most recently synchronized host
     *     and repeat the above steps until a suitable host is found.</li>
     * </ul>
     *
     * @param synState the synchronization state.
     * @param instance the instance to be scheduled.
     * @return the suitable host id for the instance.
     */
    int getSuitHostId(SynState synState, Instance instance) {
        int synPartitionId = firstPartitionId;
        if (datacenter.getStatesManager().isSynCostTime()) {
            synPartitionId = (firstPartitionId + datacenter.getStatesManager().getPartitionSynCount()) % partitionNum;
        }
        PartitionRangesManager partitionRangesManager = datacenter.getStatesManager().getPartitionRangesManager();
        int partitionNum = partitionRangesManager.getPartitionNum();

        // latest synchronization partition scheduling
        int partitionLength = partitionRangesManager.getRangeLength(synPartitionId);
        int groupNum = partitionLength / partitionNum;
        int startGroupId = random.nextInt(groupNum);
        for (int _groupPartitionId = 0; _groupPartitionId < partitionNum; _groupPartitionId++) {// Traverse the host in the group
            int groupPartitionId = (firstPartitionId + _groupPartitionId) % partitionNum;
            for (int _groupId = 0; _groupId < groupNum; _groupId++) {// Traverse the group
                int groupId = (startGroupId + _groupId) % groupNum;
                int hostId = partitionRangesManager.getRange(synPartitionId)[0] + groupId * partitionNum + groupPartitionId;
                if (synState.isSuitable(hostId, instance)) {
                    synState.allocateTmpResource(hostId, instance);
                    return hostId;
                }
            }
        }

        // Other partition scheduling
        for (int _partitionId = 1; _partitionId < partitionNum; _partitionId++) {// Traverse the partition
            int partitionId = (synPartitionId + partitionNum - _partitionId) % partitionNum;
            for (int _groupPartitionId = 0; _groupPartitionId < partitionNum; _groupPartitionId++) {// Traverse the host in the group
                int groupPartitionId = (firstPartitionId + _groupPartitionId) % partitionNum;
                groupNum = partitionRangesManager.getRangeLength(partitionId) / partitionNum;
                startGroupId = random.nextInt(groupNum);
                for (int _groupId = 0; _groupId < groupNum; _groupId++) {// Traverse the group
                    int groupId = (startGroupId + _groupId) % groupNum;
                    int hostId = partitionRangesManager.getRange(partitionId)[0] + groupId * partitionNum + groupPartitionId;
                    if (synState.isSuitable(hostId, instance)) {
                        synState.allocateTmpResource(hostId, instance);
                        return hostId;
                    }
                }
            }
        }
        return -1;
    }
}
