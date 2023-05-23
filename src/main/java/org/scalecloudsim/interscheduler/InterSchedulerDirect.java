package org.scalecloudsim.interscheduler;

import org.cloudsimplus.network.topologies.NetworkTopology;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.request.InstanceGroup;

import java.util.*;

public class InterSchedulerDirect extends InterSchedulerSimple {
    Random random = new Random(1);

    @Override
    public Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = datacenter.getSimulation().getCollaborationManager().getDatacenters(datacenter);
        NetworkTopology networkTopology = datacenter.getSimulation().getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = getAvailableDatacenters(instanceGroup, allDatacenters, networkTopology);
            instanceGroupAvaiableDatacenters.put(instanceGroup, availableDatacenters);
        }
        interScheduleByNetworkTopology(instanceGroupAvaiableDatacenters, networkTopology);
        this.filterSuitableDatacenterCostTime = 0.2;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> datacenters = instanceGroupAvaiableDatacenters.get(instanceGroup);
            Datacenter targetDatacenter = datacenters.get(random.nextInt(datacenters.size()));
            datacenters.clear();
            datacenters.add(targetDatacenter);
        }
        return instanceGroupAvaiableDatacenters;
    }

    private List<Datacenter> getAvailableDatacenters(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        //根据接入时延要求得到可调度的数据中心
        filterDatacentersByAccessLatency(instanceGroup, availableDatacenters, networkTopology);
        //根据资源抽样信息得到可调度的数据中心
        filterDatacentersByResourceSample(instanceGroup, availableDatacenters);
        return availableDatacenters;
    }

    private void filterDatacentersByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        allDatacenters.removeIf(datacenter -> instanceGroup.getAccessLatency() < networkTopology.getAcessLatency(this.datacenter, datacenter));
    }

    private void filterDatacentersByResourceSample(InstanceGroup instanceGroup, List<Datacenter> allDatacenters) {
        //首先是粗粒度地筛选总量是否满足
        allDatacenters.removeIf(
                datacenter -> datacenter.getStatesManager().getSimpleState().getCpuAvaiableSum() < instanceGroup.getCpuSum() ||
                        datacenter.getStatesManager().getSimpleState().getRamAvaiableSum() < instanceGroup.getRamSum() ||
                        datacenter.getStatesManager().getSimpleState().getStorageAvaiableSum() < instanceGroup.getStorageSum() ||
                        datacenter.getStatesManager().getSimpleState().getBwAvaiableSum() < instanceGroup.getBwSum()
        );
        //然后细粒度地查看CPU-RAM的组合是否满足
        for (Datacenter datacenter : allDatacenters) {
            Map<Integer, Map<Integer, Integer>> instanceCpuRamNum = new HashMap<>();//记录一下所有Instance的cpu—ram的种类情况
            for (Instance instance : instanceGroup.getInstanceList()) {
                int allocateNum = instanceCpuRamNum.getOrDefault(instance.getCpu(), new HashMap<>()).getOrDefault(instance.getRam(), 0);
                if (datacenter.getStatesManager().getSimpleState().getCpuRamSum(instance.getCpu(), instance.getRam()) - allocateNum <= 0) {
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
        //TODO 后续可以添加一个回溯算法来简单筛选
    }
}
