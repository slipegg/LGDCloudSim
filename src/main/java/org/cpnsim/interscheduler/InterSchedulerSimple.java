package org.cpnsim.interscheduler;

import lombok.Getter;
import lombok.Setter;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;
import org.cpnsim.statemanager.SimpleState;
import org.cpnsim.statemanager.SimpleStateEasyObject;

import java.util.*;

public class InterSchedulerSimple implements InterScheduler {
    @Getter
    @Setter
    Simulation simulation;
    @Getter
    @Setter
    int collaborationId;
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
    @Getter
    @Setter
    boolean directedSend = false;

    @Getter
    Map<Datacenter, Object> interScheduleSimpleStateMap = new HashMap<>();

    Random random = new Random(1);

    public InterSchedulerSimple(Simulation simulation, int collaborationId) {
        this.id = 0;
        this.simulation = simulation;
        this.collaborationId = collaborationId;
        this.name = "collaboration" + collaborationId + "-InterScheduler" + id;
    }

    public InterSchedulerSimple(int id, Simulation simulation, int collaborationId) {
        this.id = id;
        this.name = "collaboration" + collaborationId + "-InterScheduler" + id;
        this.simulation = simulation;
        this.collaborationId = collaborationId;
    }

    @Override
    public Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = getAvailableDatacenters(instanceGroup, allDatacenters, networkTopology);
            instanceGroupAvaiableDatacenters.put(instanceGroup, availableDatacenters);
        }
        interScheduleByNetworkTopology(instanceGroupAvaiableDatacenters, networkTopology);
        this.filterSuitableDatacenterCostTime = 0.2;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.2ms
        return instanceGroupAvaiableDatacenters;
    }

    @Override
    public Map<InstanceGroup, Double> decideReciveGroupResult(List<InstanceGroup> instanceGroups) {
        //TODO 怎么判断是否接收，如果接收了怎么进行资源预留，目前是全部接收
        Map<InstanceGroup, Double> result = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
                continue;
            }
            Double score = random.nextDouble(100);
            result.put(instanceGroup, score);
        }
        this.decideReciveGroupResultCostTime = 0.1;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
        return result;
    }

    @Override
    public Map<InstanceGroup, Datacenter> decideTargetDatacenter(Map<InstanceGroup, Map<Datacenter, Double>> instanceGroupSendResultMap, List<InstanceGroup> instanceGroups) {
        this.decideTargetDatacenterCostTime = 0.0;
        Map<InstanceGroup, Datacenter> result = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            //取每个instanceGroup中最高得分的datacenter作为调度目标
            Datacenter datacenter = instanceGroupSendResultMap.get(instanceGroup).entrySet().stream()
                    .max(Comparator.comparingDouble(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            result.put(instanceGroup, datacenter);
        }
        return result;
    }

    @Override
    public void receiveNotEmployGroup(List<InstanceGroup> instanceGroups) {
        // 目前不需要做任何处理
    }

    @Override
    public void receiveEmployGroup(List<InstanceGroup> instanceGroups) {
        // 目前不需要做任何处理
    }

    @Override
    public boolean isDirectedSend() {
        return directedSend;
    }

    //TODO 如果前一个亲和组被可能被分配给多个数据中心，那么后一个亲和组在分配的时候应该如何更新资源状态。目前是不考虑
    List<Datacenter> getAvailableDatacenters(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        //根据接入时延要求得到可调度的数据中心
        filterDatacentersByAccessLatency(instanceGroup, availableDatacenters, networkTopology);
        //根据资源抽样信息得到可调度的数据中心
        filterDatacentersByResourceSample(instanceGroup, availableDatacenters);
        return availableDatacenters;
    }

    private void filterDatacentersByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        Datacenter belongDatacenter = simulation.getCollaborationManager().getDatacenterById(instanceGroup.getUserRequest().getBelongDatacenterId());
        allDatacenters.removeIf(datacenter -> instanceGroup.getAccessLatency() < networkTopology.getAcessLatency(belongDatacenter, datacenter));
    }

    private void filterDatacentersByResourceSample(InstanceGroup instanceGroup, List<Datacenter> allDatacenters) {
        //首先是粗粒度地筛选总量是否满足
        allDatacenters.removeIf(
                datacenter -> {
                    SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);
                    return simpleStateEasyObject.getCpuAvailableSum() < instanceGroup.getCpuSum()
                            || simpleStateEasyObject.getRamAvailableSum() < instanceGroup.getRamSum()
                            || simpleStateEasyObject.getStorageAvailableSum() < instanceGroup.getStorageSum()
                            || simpleStateEasyObject.getBwAvailableSum() < instanceGroup.getBwSum();
                }
        );
        //然后细粒度地查看CPU-RAM的组合是否满足
//        Iterator<Datacenter> iterator = allDatacenters.iterator();
//        while (iterator.hasNext()) {
//            Datacenter datacenter = iterator.next();
//            Map<Integer, Map<Integer, Integer>> instanceCpuRamNum = new HashMap<>();//记录一下所有Instance的cpu—ram的种类情况
//            for (Instance instance : instanceGroup.getInstanceList()) {
//                int allocateNum = instanceCpuRamNum.getOrDefault(instance.getCpu(), new HashMap<>()).getOrDefault(instance.getRam(), 0);
//                int originSum = datacenter.getStatesManager().getSimpleState().getCpuRamSum(instance.getCpu(), instance.getRam());
//                if (originSum - allocateNum <= 0) {
//                    //如果该数据中心的资源不足以满足亲和组的资源需求，那么就将其从可调度的数据中心中移除
//                    iterator.remove();
//                    break;
//                } else {
//                    //如果该数据中心的资源可以满足亲和组的资源需求，那么就记录更新已分配的所有Instance的cpu—ram的种类情况
//                    if (instanceCpuRamNum.containsKey(instance.getCpu())) {
//                        Map<Integer, Integer> ramNumMap = instanceCpuRamNum.get(instance.getCpu());
//                        if (ramNumMap.containsKey(instance.getRam())) {
//                            ramNumMap.put(instance.getRam(), ramNumMap.get(instance.getRam()) + 1);
//                        } else {
//                            ramNumMap.put(instance.getRam(), 1);
//                        }
//                    } else {
//                        Map<Integer, Integer> ramNumMap = new HashMap<>();
//                        ramNumMap.put(instance.getRam(), 1);
//                        instanceCpuRamNum.put(instance.getCpu(), ramNumMap);
//                    }
//                }
//            }
//        }
    }

    void interScheduleByNetworkTopology(Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters, NetworkTopology networkTopology) {
        //TODO 根据网络拓扑中的时延和宽带进行筛选得到最优的调度方案
        //TODO 后续可以添加一个回溯算法来简单筛选
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
        this.name = name + "-dc" + datacenter.getId();
    }
}
