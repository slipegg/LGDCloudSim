package org.lgdcloudsim.intrascheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.network.ClosTopology;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.SynState;
import org.lgdcloudsim.util.Range;
import org.lgdcloudsim.util.utils;

public class IntraSchedulerTopologyBinPackGang extends IntraSchedulerSimple {
    /**
     * Construct the intra-scheduler with the id, the first partition id and the partition number.
     *
     * @param id               the intra-scheduler id.
     * @param firstPartitionId the first synchronization partition id.
     * @param partitionNum     the number of partitions in the data center.
     */
    public IntraSchedulerTopologyBinPackGang(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    private int getTotalAvailableGpu(ClosTopology topology, SynState synState) {
        Range hostRange = topology.getRange();
        int totalAvailableGpu = 0;
        for (int hostId = hostRange.getMin(); hostId <= hostRange.getMax(); hostId++) {
            HostState hostState = synState.getHostState(hostId);
            totalAvailableGpu += hostState.getGpu();
        }
        return totalAvailableGpu;
    }

    private void scheduleForInstanceGroup(InstanceGroup instanceGroup, SynState synState, IntraSchedulerResult intraSchedulerResult, List<ClosTopology> s0Topologies) {
        List<Instance> instances = instanceGroup.getInstances();
        s0Topologies.sort(Comparator.comparingInt((ClosTopology topology) -> getTotalAvailableGpu(topology, synState)).reversed());

        Map<Integer, HostState> hostStateMap = new HashMap<>();
        List<Integer> candidateHostIds = new ArrayList<>();
        for (Instance instance : instances) {
            for (ClosTopology topology : s0Topologies) {
                Range hostRange = topology.getRange();

                int hostId = hostRange.getMin();
                for (; hostId <= hostRange.getMax(); hostId++) {
                    HostState hostState = hostStateMap.getOrDefault(hostId, synState.getHostState(hostId));
                    if (hostState.isSuitable(instance)) {
                        hostState.allocate(instance);
                        candidateHostIds.add(hostId);
                        hostStateMap.put(hostId, hostState);
                        break;
                    }
                }

                if (hostId <= hostRange.getMax()) {
                    break;
                }
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
        
        List<ClosTopology> s0Topologies = getDatacenter().getSimulation().getNetworkTopology().getClosTopology(getDatacenter().getId()).getS0ClosTopologies();
        for (InstanceGroup instanceGroup : instanceGroups) {
            scheduleForInstanceGroup(instanceGroup, synState, intraSchedulerResult, s0Topologies);
        }
    
        return intraSchedulerResult;
    }
}
