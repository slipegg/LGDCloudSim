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
public class IntraSchedulerGPUBinPackGang extends IntraSchedulerSimple {
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
    public IntraSchedulerGPUBinPackGang(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    private void scheduleForInstanceGroup(InstanceGroup instanceGroup, SynState synState, IntraSchedulerResult intraSchedulerResult, TreeMap<Integer, List<Integer>> availableGpuToHosts) {
        List<Instance> instances = instanceGroup.getInstances();
        
        Map<Integer, HostState> scheduledHostStateMap = new HashMap<>();
        List<Integer> candidateHostIds = new ArrayList<>();
        for (Instance instance : instances) {
            int requiredGpu = instance.getGpu();
            Map.Entry<Integer, List<Integer>> entry = availableGpuToHosts.ceilingEntry(requiredGpu);
            int chosenHostId = -1;

            // 从可容纳该实例的最小剩余GPU的主机中寻找可用主机
            while (entry != null) {
                List<Integer> hostList = entry.getValue();
                int size = hostList.size();

                if (size > 0) {
                    int startIndex = random.nextInt(size); // 从随机位置开始
                    for (int j = 0; j < size; j++) {
                        int idx = (startIndex + j) % size;
                        int hostId = hostList.get(idx);
                        HostState hostState = scheduledHostStateMap.getOrDefault(hostId, synState.getHostState(hostId));
                        if (hostState.isSuitable(instance)) {
                            chosenHostId = hostId;

                            candidateHostIds.add(hostId);
                            hostState.allocate(instance);
                            scheduledHostStateMap.put(hostId, hostState);
                            hostList.remove(idx); // 从当前GPU桶中移除该主机
                            if (hostList.isEmpty()) {
                                availableGpuToHosts.remove(entry.getKey());
                            }
                            availableGpuToHosts
                                .computeIfAbsent(hostState.getGpu(), k -> new ArrayList<>())
                                .add(chosenHostId);
                            break;
                        }
                    }
                }

                if (chosenHostId != -1) {
                    break; // 找到合适的主机，跳出循环
                }

                entry = availableGpuToHosts.higherEntry(entry.getKey());
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

            for (int i = 0; i < candidateHostIds.size(); i++) {
                Instance instance = instances.get(i);
                int hostId = candidateHostIds.get(i);

                HostState hostState = scheduledHostStateMap.get(hostId);
                availableGpuToHosts.get(hostState.getGpu()).remove(hostId);
                
                hostState.release(instance);
                // 更新 availableGpuToHosts
                int availableGpu = hostState.getGpu();
                availableGpuToHosts
                    .computeIfAbsent(availableGpu, k -> new ArrayList<>())
                    .add(hostId);
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
        
        TreeMap<Integer, List<Integer>> availableGpuToHosts = new TreeMap<>();

        // 获取当前调度器视图范围内的主机
        List<Integer> innerSchedulerView = datacenter.getStatesManager().getIntraSchedulerView(this);
        int startId = innerSchedulerView.get(0);
        int endId = innerSchedulerView.get(1);
        for (int hostId = startId; hostId <= endId; hostId++) {
            int hostAvailableGpu = synState.getHostState(hostId).getGpu();
            availableGpuToHosts
                .computeIfAbsent(hostAvailableGpu, k -> new ArrayList<>())
                .add(hostId);
        }
        for (InstanceGroup instanceGroup : instanceGroups) {
            scheduleForInstanceGroup(instanceGroup, synState, intraSchedulerResult, availableGpuToHosts);
        }
    
        return intraSchedulerResult;
    }
    
}
