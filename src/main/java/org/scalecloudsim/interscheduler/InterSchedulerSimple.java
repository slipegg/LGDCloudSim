package org.scalecloudsim.interscheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.request.InstanceGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InterSchedulerSimple implements InterScheduler {
    @Getter
    Datacenter datacenter;
    @Getter
    String name;
    @Getter
    int id;

    @Getter
    double filterSuitableDatacenterCostTime = 0.0;
    @Getter
    double decideReciveGroupResultCostTime = 0.0;
    @Getter
    double decideTargetDatacenterCostTime = 0.0;

    public InterSchedulerSimple() {
        this.id = 0;
    }

    public InterSchedulerSimple(int id, Datacenter datacenter) {
        this.id = id;
        this.name = "InterScheduler" + id;
        this.datacenter = datacenter;
    }

    @Override
    public Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = datacenter.getSimulation().getCollaborationManager().getDatacenters(datacenter);
        NetworkTopology networkTopology = datacenter.getSimulation().getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = getAvaiableDatacenters(instanceGroup, allDatacenters, networkTopology);
            instanceGroupAvaiableDatacenters.put(instanceGroup, availableDatacenters);
        }
        interScheduleByNetworkTopology(instanceGroupAvaiableDatacenters, networkTopology);
        this.filterSuitableDatacenterCostTime = instanceGroups.size() * 0.1;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
        return instanceGroupAvaiableDatacenters;
    }

    @Override
    public Map<InstanceGroup, Boolean> decideReciveGroupResult(List<InstanceGroup> instanceGroups) {
        //TODO 怎么判断是否接收，如果接收了怎么进行资源预留，目前是全部接收
        Map<InstanceGroup, Boolean> result = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            result.put(instanceGroup, true);
        }
        this.decideReciveGroupResultCostTime = instanceGroups.size() * 0.1;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
        return result;
    }

    @Override
    public double getDecideReciveGroupResultCostTime() {
        return decideReciveGroupResultCostTime;
    }

    @Override
    public Datacenter decideTargetDatacenter(Map<InstanceGroup, Map<Datacenter, Integer>> instanceGroupSendResultMap, InstanceGroup instanceGroup) {
        this.decideTargetDatacenterCostTime = 0.0;
        List<Datacenter> datacenters = instanceGroupSendResultMap.get(instanceGroup).entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .toList();
        if (datacenters.size() == 0) {
            //表示调度失败
            return null;
        }
        Random random = new Random(1);
        return datacenters.get(random.nextInt(datacenters.size()));
    }

    @Override
    public void receiveNotEmployGroup(InstanceGroup instanceGroup) {
        // 目前不需要做任何处理
    }

    @Override
    public void receiveEmployGroup(InstanceGroup instanceGroup) {
        // 目前不需要做任何处理
    }

    //TODO 如果前一个亲和组被可能被分配给多个数据中心，那么后一个亲和组在分配的时候应该如何更新资源状态。目前是不考虑
    private List<Datacenter> getAvaiableDatacenters(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        //根据接入时延要求得到可调度的数据中心
        filterDatacentersByAccessLatency(instanceGroup, allDatacenters, networkTopology);
        //根据资源抽样信息得到可调度的数据中心
        filterDatacentersByResourceSample(instanceGroup, allDatacenters);
        return allDatacenters;
    }

    private void filterDatacentersByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        allDatacenters.removeIf(datacenter -> instanceGroup.getAcessLatency() < networkTopology.getAcessLatency(datacenter));
    }

    private void filterDatacentersByResourceSample(InstanceGroup instanceGroup, List<Datacenter> allDatacenters) {
        //首先是粗粒度地筛选总量是否满足
        allDatacenters.removeIf(
                datacenter -> datacenter.getStateManager().getSimpleState().getCpuAvaiableSum() < instanceGroup.getCpuSum() ||
                        datacenter.getStateManager().getSimpleState().getRamAvaiableSum() < instanceGroup.getRamSum() ||
                        datacenter.getStateManager().getSimpleState().getStorageAvaiableSum() < instanceGroup.getStorageSum() ||
                        datacenter.getStateManager().getSimpleState().getBwAvaiableSum() < instanceGroup.getBwSum()
        );
        //然后细粒度地查看CPU-RAM的组合是否满足
        for (Datacenter datacenter : allDatacenters) {
            Map<Integer, Map<Integer, Integer>> instanceCpuRamNum = new HashMap<>();//记录一下所有Instance的cpu—ram的种类情况
            for (Instance instance : instanceGroup.getInstanceList()) {
                int allocateNum = instanceCpuRamNum.getOrDefault(instance.getCpu(), new HashMap<>()).getOrDefault(instance.getRam(), 0);
                if (datacenter.getStateManager().getSimpleState().getCpuRamSum(instance.getCpu(), instance.getRam()) - allocateNum <= 0) {
                    //如果该数据中心的资源不足以满足亲和组的资源需求，那么就将其从可调度的数据中心中移除
                    allDatacenters.remove(datacenter);
                    break;
                } else {
                    //如果该数据中心的资源可以满足亲和组的资源需求，那么就记录更新已分配的所有Instance的cpu—ram的种类情况
                    if (instanceCpuRamNum.containsKey(instance.getCpu())) {
                        Map<Integer, Integer> ramNumMap = instanceCpuRamNum.get(instance.getCpu());
                        if (ramNumMap.containsKey(instance.getRam())) {
                            ramNumMap.put(instance.getRam(), ramNumMap.get(instance.getRam()) + 1);
                        } else {
                            ramNumMap.put(instance.getRam(), 1);
                        }
                    } else {
                        Map<Integer, Integer> ramNumMap = new HashMap<>();
                        ramNumMap.put(instance.getRam(), 1);
                        instanceCpuRamNum.put(instance.getCpu(), ramNumMap);
                    }
                }
            }
        }
    }

    private void interScheduleByNetworkTopology(Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters, NetworkTopology networkTopology) {
        //TODO 根据网络拓扑中的时延和宽带进行筛选得到最优的调度方案
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
        this.name = "InterScheduler" + datacenter.getId();
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }
}
