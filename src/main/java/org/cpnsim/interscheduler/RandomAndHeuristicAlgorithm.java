package org.cpnsim.interscheduler;

import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;

public class RandomAndHeuristicAlgorithm {
    public RandomAndHeuristicAlgorithm() {
    }

    // Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = RandomAndHeuristicAlgorithm.heuristicFiltering(instanceGroups, allDatacenters, RandomRate);
    public Map<InstanceGroup, List<Datacenter>> randomFiltering(List<InstanceGroup> instanceGroups, List<Datacenter> allDatacenters, Double RandomRate) {
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
        // 获取最终随机筛选出的数据中心数量，最小为1
        Integer RandomNum = Math.max(Math.ceil(RandomRate * (double)allDatacenters.size()), 1);
            
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        
            // 确保每一ms运行的两次代码不会相同
            Random random = new Random();

            // 对availableDatacenters列表进行洗牌（随机排序）
            Collections.shuffle(availableDatacenters, random);

            // 随机筛选出RandomNum个数据中心
            availableDatacenters = availableDatacenters.subList(0, RandomNum);

            instanceGroupAvailableDatacenters.put(instanceGroup, availableDatacenters);
        }
        
        return instanceGroupAvailableDatacenters;
    }

    // Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = RandomAndHeuristicAlgorithm.heuristicFiltering(instanceGroups, allDatacenters, simulation, interScheduleSimpleStateMap);
    public Map<InstanceGroup, List<Datacenter>> heuristicFiltering(List<InstanceGroup> instanceGroups, List<Datacenter> allDatacenters, Simulation simulation, Map<Datacenter, Object> interScheduleSimpleStateMap) {
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
            
        NetworkTopology networkTopology = simulation.getNetworkTopology();

        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        
            //根据接入时延要求得到可调度的数据中心
            filterDatacentersByAccessLatency(instanceGroup, availableDatacenters, networkTopology);
            //根据简单的资源抽样信息得到可调度的数据中心
            filterDatacentersByResourceSample(instanceGroup, availableDatacenters, networkTopology, interScheduleSimpleStateMap);
            //根据带宽时延要求得到可调度的数据中心
            filterAvailableDatacenterByEdgeDelayLimit(instanceGroup, availableDatacenters, networkTopology);
            filterAvailableDatacenterByEdgeBwLimit(instanceGroup, availableDatacenters, networkTopology);
            instanceGroupAvailableDatacenters.put(instanceGroup, availableDatacenters);
        }
        
        return instanceGroupAvailableDatacenters;
    }

    private void filterDatacentersByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, NetworkTopology networkTopology) {
        // Filter based on access latency
        availableDatacenters.removeIf(
                datacenter -> instanceGroup.getAccessLatency() <= networkTopology.getAccessLatency(instanceGroup.getUserRequest(), datacenter));
    }

    private void filterDatacentersByResourceSample(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, Map<Datacenter, Object> interScheduleSimpleStateMap) {
        //粗粒度地筛选总量是否满足
        availableDatacenters.removeIf(
                datacenter -> {
                    SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);
                    return simpleStateEasyObject.getCpuAvailableSum() < instanceGroup.getCpuSum()
                            || simpleStateEasyObject.getRamAvailableSum() < instanceGroup.getRamSum()
                            || simpleStateEasyObject.getStorageAvailableSum() < instanceGroup.getStorageSum()
                            || simpleStateEasyObject.getBwAvailableSum() < instanceGroup.getBwSum();
                }
        );
    }

    private void filterAvailableDatacenterByEdgeDelayLimit(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, NetworkTopology networkTopology) {
        // // 获取所有与当前instanceGroup有Edge的instanceGroup
        for (InstanceGroup dstInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup)) {
            Datacenter scheduledDatacenter = dstInstanceGroup.getReceiveDatacenter();
            if (scheduledDatacenter != Datacenter.NULL) {
                availableDatacenters.removeIf(dc -> networkTopology.getDelay(dc, scheduledDatacenter) > instanceGroup.getUserRequest().getInstanceGroupGraph().getDelay(instanceGroup, dstInstanceGroup));
            }
        }
        for (InstanceGroup srcInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup)) {
            Datacenter scheduledDatacenter = srcInstanceGroup.getReceiveDatacenter();
            if (scheduledDatacenter != Datacenter.NULL) {
                availableDatacenters.removeIf(dc -> networkTopology.getDelay(dc, scheduledDatacenter) > instanceGroup.getUserRequest().getInstanceGroupGraph().getDelay(srcInstanceGroup, instanceGroup));
            }
        }
    }

    private void filterAvailableDatacenterByEdgeBwLimit(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, NetworkTopology networkTopology) {
        // 获取所有与当前instanceGroup有Edge的instanceGroup
        for (InstanceGroup dstInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup)) {
            Datacenter scheduledDatacenter = dstInstanceGroup.getReceiveDatacenter();
            if (scheduledDatacenter != Datacenter.NULL) {
                availableDatacenters.removeIf(dc -> networkTopology.getBw(dc, scheduledDatacenter) < instanceGroup.getUserRequest().getInstanceGroupGraph().getBw(instanceGroup, dstInstanceGroup));
            }
        }
        for (InstanceGroup srcInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup)) {
            Datacenter scheduledDatacenter = srcInstanceGroup.getReceiveDatacenter();
            if (scheduledDatacenter != Datacenter.NULL) {
                availableDatacenters.removeIf(dc -> networkTopology.getBw(dc, scheduledDatacenter) < instanceGroup.getUserRequest().getInstanceGroupGraph().getBw(srcInstanceGroup, instanceGroup));
            }
        }
    }

    // RandomAndHeuristicAlgorithm.randomScoring(interSchedulerResult, instanceGroupAvailableDatacenters);
    public void randomScoring(InterSchedulerResult interSchedulerResult, Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters) {
        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleRes : instanceGroupAvailableDatacenters.entrySet()) {
            if (scheduleRes.getValue().size() == 0) {
                interSchedulerResult.getFailedInstanceGroups().add(scheduleRes.getKey());
            } else {
                // 确保每一ms运行的两次代码不会相同
                Random random = new Random();
                Datacenter target = scheduleRes.getValue().get(random.nextInt(scheduleRes.getValue().size()));
                interSchedulerResult.addDcResult(scheduleRes.getKey(), target);
            }
        }
    }

    public void heuristicScoring(InterSchedulerResult interSchedulerResult, Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters, Map<Datacenter, Object> interScheduleSimpleStateMap) {
        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleRes : instanceGroupAvailableDatacenters.entrySet()) {
            if (scheduleRes.getValue().size() == 0) {
                interSchedulerResult.getFailedInstanceGroups().add(scheduleRes.getKey());
            } else {
                // 对数据中心排序
                Collections.sort(scheduleRes.getValue(), (d1, d2) -> {
                    double scoreD1 = getScoreForDc(scheduleRes.getKey(), d1, (SimpleStateEasyObject)interScheduleSimpleStateMap.get(d1));
                    double scoreD2 = getScoreForDc(scheduleRes.getKey(), d2, (SimpleStateEasyObject)interScheduleSimpleStateMap.get(d2));
    
                    return Double.compare(scoreD1, scoreD2);
                });

                Datacenter target = scheduleRes.getValue().get(0);
                interSchedulerResult.addDcResult(scheduleRes.getKey(), target);
            }
        }
    }

    private double getScoreForDc(InstanceGroup instanceGroup, Datacenter datacenter, SimpleStateEasyObject simpleStateEasyObject) {
        long cpuSum = instanceGroup.getCpuSum();
        long ramSum = instanceGroup.getRamSum();
        long storageSum = instanceGroup.getStorageSum();
        long bwSum = instanceGroup.getBwSum();
        if(simpleStateEasyObject.getCpuAvailableSum()<cpuSum || simpleStateEasyObject.getRamAvailableSum()<ramSum || simpleStateEasyObject.getStorageAvailableSum()<storageSum || simpleStateEasyObject.getBwAvailableSum()<bwSum){
            return -1;
        }else{
            double score = (simpleStateEasyObject.getCpuAvailableSum() * 10 / (double) simpleStateEasyObject.getCpuCapacitySum() + simpleStateEasyObject.getRamAvailableSum() * 10 / (double) simpleStateEasyObject.getRamCapacitySum()) / 2;
            scoreDcHistoryMap.put(datacenter, score);
            return score;
        }
    }
}