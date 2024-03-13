package org.cpnsim.interscheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cpnsim.core.CloudInformationService;
import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.network.NetworkTopology;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.statemanager.SimpleStateEasyObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomAndHeuristicAlgorithm {
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAndHeuristicAlgorithm.class.getSimpleName());


    public RandomAndHeuristicAlgorithm() {
    }

    // Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = RandomAndHeuristicAlgorithm.heuristicFiltering(instanceGroups, allDatacenters, RandomRate);
    public static Map<InstanceGroup, List<Datacenter>> randomFiltering(List<InstanceGroup> instanceGroups, List<Datacenter> allDatacenters, Double RandomRate) {
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
        // 获取最终随机筛选出的数据中心数量，最小为1
        Integer RandomNum = (int) Math.max(Math.ceil(RandomRate * (double)allDatacenters.size()), 1);
            
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        
            // 确保每一ms运行的两次代码不会相同
            Random random = new Random();

            // 对availableDatacenters列表进行洗牌（随机排序）
            Collections.shuffle(availableDatacenters, random);

            // 随机筛选出RandomNum个数据中心
            availableDatacenters = availableDatacenters.subList(0, RandomNum);

            instanceGroupAvailableDatacenters.put(instanceGroup, availableDatacenters);
            
            // // TODO: test，待删除
            // LOGGER.warn("instanceGroup {} filted dc num: {}.",instanceGroup.getId(),availableDatacenters.size());
        }
        
        return instanceGroupAvailableDatacenters;
    }

    // Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = RandomAndHeuristicAlgorithm.heuristicFiltering(instanceGroups, allDatacenters, simulation, interScheduleSimpleStateMap);
    public static Map<InstanceGroup, List<Datacenter>> heuristicFiltering(List<InstanceGroup> instanceGroups, List<Datacenter> allDatacenters, Simulation simulation, Map<Datacenter, Object> interScheduleSimpleStateMap) {
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
            
        NetworkTopology networkTopology = simulation.getNetworkTopology();

        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        
            //根据接入时延要求得到可调度的数据中心
            filterDatacentersByAccessLatency(instanceGroup, availableDatacenters, networkTopology);
            //根据简单的资源抽样信息得到可调度的数据中心
            filterDatacentersByResourceSample(instanceGroup, availableDatacenters, interScheduleSimpleStateMap);
            //根据带宽时延要求得到可调度的数据中心
            filterAvailableDatacenterByEdgeDelayLimit(instanceGroup, availableDatacenters, networkTopology);
            filterAvailableDatacenterByEdgeBwLimit(instanceGroup, availableDatacenters, networkTopology);
            instanceGroupAvailableDatacenters.put(instanceGroup, availableDatacenters);

            // // TODO: test，待删除
            // LOGGER.warn("instanceGroup {} filted dc num: {}.",instanceGroup.getId(),availableDatacenters.size());
        }
        
        return instanceGroupAvailableDatacenters;
    }

    private static void filterDatacentersByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, NetworkTopology networkTopology) {
        // Filter based on access latency
        availableDatacenters.removeIf(
                datacenter -> instanceGroup.getAccessLatency() <= networkTopology.getAccessLatency(instanceGroup.getUserRequest(), datacenter));
    }

    private static void filterDatacentersByResourceSample(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, Map<Datacenter, Object> interScheduleSimpleStateMap) {
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

    private static void filterAvailableDatacenterByEdgeDelayLimit(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, NetworkTopology networkTopology) {
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

    private static void filterAvailableDatacenterByEdgeBwLimit(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters, NetworkTopology networkTopology) {
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
    public static void randomScoring(InterSchedulerResult interSchedulerResult, Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters) {
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

    public static void heuristicScoring(InterSchedulerResult interSchedulerResult, Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters, Map<Datacenter, Object> interScheduleSimpleStateMap) {
        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleRes : instanceGroupAvailableDatacenters.entrySet()) {
            if (scheduleRes.getValue().size() == 0) {
                interSchedulerResult.getFailedInstanceGroups().add(scheduleRes.getKey());
            } else {
                // 对数据中心排序
                Collections.sort(scheduleRes.getValue(), (d1, d2) -> {
                    double scoreD1 = getScoreForDc(scheduleRes.getKey(), d1, (SimpleStateEasyObject)interScheduleSimpleStateMap.get(d1));
                    double scoreD2 = getScoreForDc(scheduleRes.getKey(), d2, (SimpleStateEasyObject)interScheduleSimpleStateMap.get(d2));
    
                    // 按降序排序
                    return -Double.compare(scoreD1, scoreD2);
                });
                
                // // TODO: test，待删除
                // if(scheduleRes.getValue().size()>=3) {
                //     LOGGER.warn("instanceGroup {} scored 1st-3rd dcs: {} - {} - {}.",scheduleRes.getKey().getId(),scheduleRes.getValue().get(0).getId(),scheduleRes.getValue().get(1).getId(),scheduleRes.getValue().get(2).getId());
                // } else if(scheduleRes.getValue().size()==2) {
                //     LOGGER.warn("instanceGroup {} scored 1st-2nd dcs: {} - {}.",scheduleRes.getKey().getId(),scheduleRes.getValue().get(0).getId(),scheduleRes.getValue().get(1).getId());
                // } else if(scheduleRes.getValue().size()==1) {
                //     LOGGER.warn("instanceGroup {} scored 1st dc: {}.",scheduleRes.getKey().getId(),scheduleRes.getValue().get(0).getId());
                // }

                Datacenter target = scheduleRes.getValue().get(0);
                interSchedulerResult.addDcResult(scheduleRes.getKey(), target);
            }
        }
    }

    private static double getScoreForDc(InstanceGroup instanceGroup, Datacenter datacenter, SimpleStateEasyObject simpleStateEasyObject) {
        long cpuSum = instanceGroup.getCpuSum();
        long ramSum = instanceGroup.getRamSum();
        long storageSum = instanceGroup.getStorageSum();
        long bwSum = instanceGroup.getBwSum();
        if(simpleStateEasyObject.getCpuAvailableSum()<cpuSum || simpleStateEasyObject.getRamAvailableSum()<ramSum || simpleStateEasyObject.getStorageAvailableSum()<storageSum || simpleStateEasyObject.getBwAvailableSum()<bwSum){
            return -1;
        }else{
            double score = (simpleStateEasyObject.getCpuAvailableSum() * 10 / (double) simpleStateEasyObject.getCpuCapacitySum() + simpleStateEasyObject.getRamAvailableSum() * 10 / (double) simpleStateEasyObject.getRamCapacitySum()) / 2;
            return score;
        }
    }
}