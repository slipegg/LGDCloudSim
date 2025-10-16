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
public class IntraSchedulerGPUBinPack extends IntraSchedulerSimple {
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
    public IntraSchedulerGPUBinPack(int id, int firstPartitionId, int partitionNum) {
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
        IntraSchedulerResult intraSchedulerResult =
                new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());
    
        // 获取当前调度器视图范围内的主机
        List<Integer> innerSchedulerView = datacenter.getStatesManager().getIntraSchedulerView(this);
        int startId = innerSchedulerView.get(0);
        int endId = innerSchedulerView.get(1);
    
        // === Step 1: 初始化 TreeMap，key 为剩余 GPU，value 为 hostId 列表 ===
        TreeMap<Integer, List<Integer>> availableGpuToHosts = new TreeMap<>();
    
        for (int hostId = startId; hostId <= endId; hostId++) {
            int hostAvailableGpu = synState.getHostState(hostId).getGpu();
            availableGpuToHosts
                .computeIfAbsent(hostAvailableGpu, k -> new ArrayList<>())
                .add(hostId);
        }
    
        // === Step 2: 调度每个实例 ===
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
                        if (synState.isSuitable(hostId, instance)) {
                            chosenHostId = hostId;
                            hostList.remove(idx); // 从当前GPU桶中移除该主机
                            if (hostList.isEmpty()) {
                                availableGpuToHosts.remove(entry.getKey());
                            }
                            break;
                        }
                    }
                }

                if (chosenHostId != -1) {
                    break; // 找到合适的主机，跳出循环
                }
                
                entry = availableGpuToHosts.higherEntry(entry.getKey());
            }
    
            // === Step 3: 分配或标记失败 ===
            if (chosenHostId != -1) {
                synState.allocateTmpResource(chosenHostId, instance);
                instance.setExpectedScheduleHostId(chosenHostId);
                intraSchedulerResult.addScheduledInstance(instance);
    
                // 更新该主机剩余 GPU
                int newAvailableGpu = synState.getHostState(chosenHostId).getGpu();
                availableGpuToHosts
                    .computeIfAbsent(newAvailableGpu, k -> new ArrayList<>())
                    .add(chosenHostId);
            } else {
                intraSchedulerResult.addFailedScheduledInstance(instance);
            }
        }
    
        return intraSchedulerResult;
    }
    
}
