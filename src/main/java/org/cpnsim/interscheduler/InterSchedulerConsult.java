package org.cpnsim.interscheduler;

import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupEdge;
import org.cpnsim.request.UserRequest;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.SimpleStateEasyObject;

import java.util.*;
import java.util.stream.IntStream;

public class InterSchedulerConsult extends InterSchedulerSimple {
    Random random = new Random();

    public InterSchedulerConsult(int id, Simulation simulation, int collaborationId) {
        super(id, simulation, collaborationId);
    }

    class DcRecorder {
        long allocatedCpuSum = 0;
        long allocatedRamSum = 0;
        long allocatedStorageSum = 0;
        long allocatedBwSum = 0;

        public void updateResourceAllocated(InstanceGroup instanceGroup) {
            allocatedCpuSum += instanceGroup.getCpuSum();
            allocatedRamSum += instanceGroup.getRamSum();
            allocatedStorageSum += instanceGroup.getStorageSum();
            allocatedBwSum += instanceGroup.getBwSum();
        }

        public void updateResourceAllocated(InstanceGroup instanceGroup, double probability) {
            allocatedCpuSum += instanceGroup.getCpuSum() * probability;
            allocatedRamSum += instanceGroup.getRamSum() * probability;
            allocatedStorageSum += instanceGroup.getStorageSum() * probability;
            allocatedBwSum += instanceGroup.getBwSum() * probability;
        }
    }

    class AllocatedRecorder {
        Map<Datacenter, DcRecorder> dcRecorderMap = new HashMap<>();

        Map<Datacenter, Map<Datacenter, Double>> allocatedBwMap = new HashMap<>();

        public void updateAllocatedBw(Datacenter srcDc, Datacenter dstDc, double bw) {
            allocatedBwMap.putIfAbsent(srcDc, new HashMap<>());
            allocatedBwMap.get(srcDc).putIfAbsent(dstDc, 0.0);
            allocatedBwMap.get(srcDc).put(dstDc, allocatedBwMap.get(srcDc).get(dstDc) + bw);
        }

        public double getAllocatedBw(Datacenter srcDc, Datacenter dstDc) {
            if (allocatedBwMap.containsKey(srcDc)) {
                if (allocatedBwMap.get(srcDc).containsKey(dstDc)) {
                    return allocatedBwMap.get(srcDc).get(dstDc);
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }

        public void updateResourceAllocated(InstanceGroup instanceGroup, Datacenter datacenter) {
            dcRecorderMap.putIfAbsent(datacenter, new DcRecorder());
            dcRecorderMap.get(datacenter).updateResourceAllocated(instanceGroup);
        }

        public void updateResourceAllocated(InstanceGroup instanceGroup, List<Datacenter> datacenters) {
            for (Datacenter dc : datacenters) {
                dcRecorderMap.putIfAbsent(dc, new DcRecorder());
                dcRecorderMap.get(dc).updateResourceAllocated(instanceGroup, 1.0 / datacenters.size());
            }
        }

        public long getAllocatedCpuSum(Datacenter datacenter) {
            if (dcRecorderMap.containsKey(datacenter)) {
                return dcRecorderMap.get(datacenter).allocatedCpuSum;
            } else {
                return 0;
            }
        }

        public long getAllocatedRamSum(Datacenter datacenter) {
            if (dcRecorderMap.containsKey(datacenter)) {
                return dcRecorderMap.get(datacenter).allocatedRamSum;
            } else {
                return 0;
            }
        }

        public long getAllocatedStorageSum(Datacenter datacenter) {
            if (dcRecorderMap.containsKey(datacenter)) {
                return dcRecorderMap.get(datacenter).allocatedStorageSum;
            } else {
                return 0;
            }
        }

        public long getAllocatedBwSum(Datacenter datacenter) {
            if (dcRecorderMap.containsKey(datacenter)) {
                return dcRecorderMap.get(datacenter).allocatedBwSum;
            } else {
                return 0;
            }
        }
    }

    @Override
    public Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
        AllocatedRecorder allocatedRecorder = new AllocatedRecorder();

        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);

            // Filter based on access latency
            Datacenter belongDatacenter = simulation.getCollaborationManager().getDatacenterById(instanceGroup.getUserRequest().getBelongDatacenterId());
            availableDatacenters.removeIf(
                    datacenter -> instanceGroup.getAccessLatency() < networkTopology.getAcessLatency(belongDatacenter, datacenter));

            // Filter based on the total remaining resources
            availableDatacenters.removeIf(
                    datacenter -> {
                        SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);
                        return instanceGroup.getCpuSum() > simpleStateEasyObject.getCpuAvailableSum() - allocatedRecorder.getAllocatedCpuSum(datacenter)
                                || instanceGroup.getRamSum() > simpleStateEasyObject.getRamAvailableSum() - allocatedRecorder.getAllocatedRamSum(datacenter)
                                || instanceGroup.getStorageSum() > simpleStateEasyObject.getStorageAvailableSum() - allocatedRecorder.getAllocatedStorageSum(datacenter)
                                || instanceGroup.getBwSum() > simpleStateEasyObject.getBwAvailableSum() - allocatedRecorder.getAllocatedBwSum(datacenter);
                    });

            // Filter based on the instanceGroupGraph and network topology
            List<InstanceGroup> dstInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup);
            for (InstanceGroup dstInstanceGroup : dstInstanceGroups) {
                Datacenter dstReceiveDc = dstInstanceGroup.getReceiveDatacenter();
                if (dstReceiveDc != Datacenter.NULL) {
                    Iterator<Datacenter> iterator2 = availableDatacenters.iterator();
                    while (iterator2.hasNext()) {
                        Datacenter datacenter = iterator2.next();
                        InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(instanceGroup, dstInstanceGroup);
                        if (networkTopology.getDelay(datacenter, dstReceiveDc) > edge.getMinDelay()) {
                            iterator2.remove();
                            continue;
                        }
                        if (networkTopology.getBw(datacenter, dstReceiveDc) - allocatedRecorder.getAllocatedBw(datacenter, dstReceiveDc) < edge.getRequiredBw()) {
                            iterator2.remove();
                        }
                    }
                }
            }
            List<InstanceGroup> srcInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup);
            for (InstanceGroup srcInstanceGroup : srcInstanceGroups) {
                Datacenter srcReceiveDc = srcInstanceGroup.getReceiveDatacenter();
                if (srcReceiveDc != Datacenter.NULL) {
                    Iterator<Datacenter> iterator2 = availableDatacenters.iterator();
                    while (iterator2.hasNext()) {
                        Datacenter datacenter = iterator2.next();
                        if (networkTopology.getDelay(datacenter, srcReceiveDc) > srcInstanceGroup.getAccessLatency()) {
                            iterator2.remove();
                            continue;
                        }
                        if (networkTopology.getBw(datacenter, srcReceiveDc) - allocatedRecorder.getAllocatedBw(datacenter, srcReceiveDc) < srcInstanceGroup.getBwSum()) {
                            iterator2.remove();
                        }
                    }
                }
            }

            // select one datacenter which cpu+0.5*ram is the max one from the available datacenters
//            availableDatacenters.sort(Comparator.comparingLong(datacenter -> {
//                SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);
//                return (simpleStateEasyObject.getCpuAvailableSum() - allocatedRecorder.getAllocatedCpuSum(datacenter)) / datacenter.getStatesManager().getMaxCpuCapacity()
//                        + (simpleStateEasyObject.getRamAvailableSum() - allocatedRecorder.getAllocatedRamSum(datacenter)) / datacenter.getStatesManager().getMaxRamCapacity()
//                        + 10 * simpleStateEasyObject.getAvgSimpleHostStateCpu()
////                        + 10L * (datacenter.getStatesManager().getHostNum() - datacenter.getStatesManager().getDatacenterPowerOnRecord().getNowPowerOnHostNum());
//            }));
            List<Double> availableSumSoftmax = new ArrayList<>();
            long cpuRamSum = 0L;
            for (Datacenter datacenter : availableDatacenters) {
                SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);
                cpuRamSum += simpleStateEasyObject.getCpuAvailableSum() - allocatedRecorder.getAllocatedCpuSum(datacenter) + simpleStateEasyObject.getRamAvailableSum() - allocatedRecorder.getAllocatedRamSum(datacenter);
            }
            for (Datacenter datacenter : availableDatacenters) {
                SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);
                availableSumSoftmax.add(((simpleStateEasyObject.getCpuAvailableSum() - allocatedRecorder.getAllocatedCpuSum(datacenter))
                        + (simpleStateEasyObject.getRamAvailableSum() - allocatedRecorder.getAllocatedRamSum(datacenter))) / (double) cpuRamSum);
            }
            List<Double> simpleAvgCpuRamSoftmax = new ArrayList<>();
            double simpleAvgCpuRamSum = 0L;
            for (Datacenter datacenter : availableDatacenters) {
                SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);
                simpleAvgCpuRamSum += (simpleStateEasyObject.getCpuAvailableSum() - allocatedRecorder.getAllocatedCpuSum(datacenter)) / (double) simpleStateEasyObject.getHostNum()
                        + (simpleStateEasyObject.getRamAvailableSum() - allocatedRecorder.getAllocatedRamSum(datacenter)) / (double) simpleStateEasyObject.getHostNum();
            }
            for (Datacenter datacenter : availableDatacenters) {
                SimpleStateEasyObject simpleStateEasyObject = (SimpleStateEasyObject) interScheduleSimpleStateMap.get(datacenter);
                simpleAvgCpuRamSoftmax.add(((simpleStateEasyObject.getCpuAvailableSum() - allocatedRecorder.getAllocatedCpuSum(datacenter)) / simpleStateEasyObject.getHostNum()
                        + (simpleStateEasyObject.getRamAvailableSum() - allocatedRecorder.getAllocatedRamSum(datacenter)) / simpleStateEasyObject.getHostNum()) / simpleAvgCpuRamSum);
            }

            Comparator<Double> reversedComparator = Comparator.reverseOrder();

            // 创建 TreeMap，并传入自定义比较器
            Map<Double, Datacenter> availableSoftmaxMap = new TreeMap<>(reversedComparator);
            for (int i = 0; i < availableDatacenters.size(); i++) {
                availableSoftmaxMap.put(availableSumSoftmax.get(i) + 2 * simpleAvgCpuRamSoftmax.get(i), availableDatacenters.get(i));
            }

            List<Datacenter> selectDatacenters = new ArrayList<>();
            for (Datacenter datacenter : availableSoftmaxMap.values()) {
                selectDatacenters.add(datacenter);
                if (selectDatacenters.size() >= 3) {
                    break;
                }
            }
            if (selectDatacenters.size() == 0) {
                instanceGroupAvailableDatacenters.put(instanceGroup, new ArrayList<>());
            } else {
                // update result
                instanceGroupAvailableDatacenters.put(instanceGroup, selectDatacenters);

                // update recorder
                allocatedRecorder.updateResourceAllocated(instanceGroup, selectDatacenters);
            }
        }
        this.filterSuitableDatacenterCostTime = 0.1 * instanceGroups.size();//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
        return instanceGroupAvailableDatacenters;
    }

    @Override
    public Map<InstanceGroup, Double> decideReciveGroupResult(List<InstanceGroup> instanceGroups) {
        Map<InstanceGroup, Double> result = new HashMap<>();
        List<HostState> simpleHostStates = simpleHostState(100);
        for (InstanceGroup instanceGroup : instanceGroups) {
            if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
                continue;
            }
            Double score = markScore(instanceGroup, simpleHostStates);
            result.put(instanceGroup, score);
        }
        this.decideReciveGroupResultCostTime = 0.01 * instanceGroups.size();//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
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

    private List<HostState> simpleHostState(int num) {
        List<HostState> simpleHostStates = new ArrayList<>();
        Set<Integer> simpleHostIds = new HashSet<>();
        int hostNum = datacenter.getStatesManager().getHostNum();
        int hostId = -1;
        for (int i = 0; i < num && i < hostNum; i++) {
            hostId = random.nextInt(hostNum);
            while (simpleHostIds.contains(hostId)) {
                hostId = (hostId + 1) % hostNum;
            }
            simpleHostIds.add(hostId);
            simpleHostStates.add(datacenter.getStatesManager().getNowHostState(hostId));
        }
        return simpleHostStates;
    }

    private double markScore(InstanceGroup instanceGroup, List<HostState> hostStates) {
        int suitHostNum = 0;
        for (Instance instance : instanceGroup.getInstanceList()) {
            for (HostState hostState : hostStates) {
                if (hostState.isSuitable(instance)) {
                    suitHostNum++;
                }
            }
        }
        return suitHostNum / (double) hostStates.size() * instanceGroup.getInstanceList().size();
    }
}
