package org.cpnsim.interscheduler;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.QueueResult;
import org.cpnsim.network.NetworkTopology;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.InstanceGroupQueue;
import org.cpnsim.datacenter.InstanceGroupQueueFifo;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;
import org.cpnsim.statemanager.DetailedDcStateSimple;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.SimpleStateEasyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InterSchedulerSimple implements InterScheduler {
    public static final int NULL = -1;
    public static final int DC_TARGET = 0;
    public static final int HOST_TARGET = 1;
    public static final int MIXED_TARGET = 2;
    public Logger LOGGER = LoggerFactory.getLogger(InterSchedulerSimple.class.getName());
    int target;
    boolean isSupportForward;
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
    double scheduleTime = 0.0;
    @Getter
    double decideReceiveGroupResultCostTime = 0.0;
    @Getter
    double decideTargetDatacenterCostTime = 0.0;
    @Getter
    @Setter
    boolean directedSend = false;

    @Getter
    InstanceGroupQueue instanceGroupQueue = new InstanceGroupQueueFifo();

    @Getter
    InstanceGroupQueue retryInstanceGroupQueue = new InstanceGroupQueueFifo();

    @Getter
    @Setter
    Map<Datacenter, Double> dcStateSynInterval = new HashMap<>();
    @Getter
    @Setter
    private Map<Datacenter, String> dcStateSynType = new HashMap<>();
    @Getter
    Map<Datacenter, Object> interScheduleSimpleStateMap = new HashMap<>();

    Random random = new Random();

    @Getter
    int traversalTime = 0;

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

    public InterSchedulerSimple(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        this.id = id;
        this.name = "collaboration" + collaborationId + "-InterScheduler" + id;
        this.simulation = simulation;
        this.collaborationId = collaborationId;
        this.target = target;
        this.isSupportForward = isSupportForward;
    }

    @Override
    public Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = getAvailableDatacenters(instanceGroup, allDatacenters, networkTopology);
            instanceGroupAvailableDatacenters.put(instanceGroup, availableDatacenters);
        }
        interScheduleByNetworkTopology(instanceGroupAvailableDatacenters, networkTopology);
        this.scheduleTime = 0.2;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.2ms
        return instanceGroupAvailableDatacenters;
    }

    private Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenterByEasySimple(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
            //根据接入时延要求得到可调度的数据中心
            filterDatacentersByAccessLatency(instanceGroup, availableDatacenters, networkTopology);
            //根据简单的资源抽样信息得到可调度的数据中心
            filterDatacentersByResourceSample(instanceGroup, availableDatacenters);
            instanceGroupAvailableDatacenters.put(instanceGroup, availableDatacenters);
        }
        interScheduleByNetworkTopology(instanceGroupAvailableDatacenters, networkTopology);
        return instanceGroupAvailableDatacenters;
    }

    protected Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenterByNetwork(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
            //根据接入时延要求得到可调度的数据中心
            filterDatacentersByAccessLatency(instanceGroup, availableDatacenters, networkTopology);
            instanceGroupAvailableDatacenters.put(instanceGroup, availableDatacenters);
        }
        interScheduleByNetworkTopology(instanceGroupAvailableDatacenters, networkTopology);
        return instanceGroupAvailableDatacenters;
    }

    @Override
    public InterSchedulerResult schedule(List<InstanceGroup> instanceGroups) {
        synDcStateRealTime();

        if (target == DC_TARGET) {
            return scheduleToDatacenter(instanceGroups);
        } else if (target == HOST_TARGET) {
            return scheduleToHost(instanceGroups);
        } else {
            throw new IllegalStateException("InterSchedulerSimple.schedule: Invalid target of " + target);
        }
    }

    @Override
    public InterSchedulerResult schedule() {
        synDcStateRealTime();

        QueueResult<InstanceGroup> queueResult = getWaitSchedulingInstanceGroups();
        List<InstanceGroup> waitSchedulingInstanceGroups = queueResult.getWaitScheduledItems();
        InterSchedulerResult interSchedulerResult = null;

        traversalTime = 0;
        double start = System.currentTimeMillis();
        if (target == DC_TARGET) {
            interSchedulerResult = scheduleToDatacenter(waitSchedulingInstanceGroups);
        } else if (target == HOST_TARGET) {
            interSchedulerResult = scheduleToHost(waitSchedulingInstanceGroups);
        } else if (target == MIXED_TARGET) {
            interSchedulerResult = scheduleMixed(waitSchedulingInstanceGroups);
        } else {
            throw new IllegalStateException("InterSchedulerSimple.schedule: Invalid target of " + target);
        }
        double end = System.currentTimeMillis();

        this.scheduleTime = Math.max(0.1, end - start);

        if(end-start<0.1) {
            LOGGER.warn("{}: interSchedule schedule time is less than 0.1 ms ({} ms).", simulation.clockStr(), end-start);
        }

        interSchedulerResult.setOutDatedUserRequests(queueResult.getOutDatedItems());
        return interSchedulerResult;
    }

    protected InterSchedulerResult scheduleMixed(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, isSupportForward, allDatacenters);
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = filterSuitableDatacenterByNetwork(instanceGroups);

        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleResEntry : instanceGroupAvailableDatacenters.entrySet()) {
            InstanceGroup instanceGroupToBeScheduled = scheduleResEntry.getKey();
            List<Datacenter> availableDatacenters = scheduleResEntry.getValue();

            if (availableDatacenters.size() == 0) {
                interSchedulerResult.getFailedInstanceGroups().add(instanceGroupToBeScheduled);
            } else {
                Datacenter scheduleResult = scheduleMixedInstanceGroup(instanceGroupToBeScheduled, availableDatacenters);

                if (scheduleResult == Datacenter.NULL) {
                    interSchedulerResult.getFailedInstanceGroups().add(instanceGroupToBeScheduled);
                } else {
                    interSchedulerResult.addDcResult(instanceGroupToBeScheduled, scheduleResult);
                }
            }
        }

        return interSchedulerResult;
    }

    private Datacenter scheduleMixedInstanceGroup(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters) {
        if (availableDatacenters.contains(datacenter)) {
            boolean isScheduleToSelfSuccess = scheduleHostInDcForInstanceGroup(instanceGroup, datacenter);
            if (isScheduleToSelfSuccess) {
                return datacenter;
            } else {
                availableDatacenters.remove(datacenter);
            }
        }

        return selectDcToForward(instanceGroup, availableDatacenters);
    }

    private Datacenter selectDcToForward(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters) {
        if (availableDatacenters.size() == 0) {
            return Datacenter.NULL;
        }

        int historyForwardDcLength = instanceGroup.getForwardDatacenterIdsHistory().size();
        int collaborationId = simulation.getCollaborationManager().getOnlyCollaborationId(datacenter.getId());
        int datacenterNumInCollaboration = simulation.getCollaborationManager().getDatacenters(collaborationId).size();
        if (historyForwardDcLength >= datacenterNumInCollaboration - 1) {
            return Datacenter.NULL;
        }

        int dcSelectedIndex = random.nextInt(availableDatacenters.size());
        return availableDatacenters.get(dcSelectedIndex);
    }

    private QueueResult<InstanceGroup> getWaitSchedulingInstanceGroups() {
        double nowTime = getSimulation().clock();
        if (retryInstanceGroupQueue.isEmpty()) {
            return instanceGroupQueue.getBatchItem(nowTime);
        } else {
            QueueResult<InstanceGroup> queueResult = retryInstanceGroupQueue.getBatchItem(nowTime);

            if (queueResult.getWaitScheduledItemsSize() < instanceGroupQueue.getBatchNum()) {
                int itemNum = instanceGroupQueue.getBatchNum() - queueResult.getWaitScheduledItemsSize();
                QueueResult<InstanceGroup> queueResultTmp = instanceGroupQueue.getItems(itemNum, nowTime);
                queueResult.add(queueResultTmp);
            }
            return queueResult;
        }
    }

    private void synDcStateRealTime() {
        List<Datacenter> realTimeSynDcList = simulation.getCollaborationManager().getDatacenters(collaborationId);
        for (Map.Entry<Datacenter, Double> dcStateSynIntervalEntry : dcStateSynInterval.entrySet()) {
            Datacenter datacenter = dcStateSynIntervalEntry.getKey();
            double interval = dcStateSynIntervalEntry.getValue();

            if (interval != 0) {
                realTimeSynDcList.remove(datacenter);
            }
        }

        synBetweenDcState(realTimeSynDcList);
    }

    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, isSupportForward, allDatacenters);
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = filterSuitableDatacenterByNetwork(instanceGroups);
        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleRes : instanceGroupAvailableDatacenters.entrySet()) {
            if (scheduleRes.getValue().size() == 0) {
                interSchedulerResult.getFailedInstanceGroups().add(scheduleRes.getKey());
            } else {
                Datacenter target = scheduleRes.getValue().get(random.nextInt(scheduleRes.getValue().size()));
                interSchedulerResult.addDcResult(scheduleRes.getKey(), target);
            }
        }

        return interSchedulerResult;
    }

    protected InterSchedulerResult scheduleToHost(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, allDatacenters);

        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = filterSuitableDatacenterByNetwork(instanceGroups);

        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleResEntry : instanceGroupAvailableDatacenters.entrySet()) {
            InstanceGroup instanceGroupToBeScheduled = scheduleResEntry.getKey();
            List<Datacenter> availableDatacenters = scheduleResEntry.getValue();
            if (availableDatacenters.size() == 0) {
                interSchedulerResult.getFailedInstanceGroups().add(instanceGroupToBeScheduled);
            } else {
                Datacenter scheduledDc = scheduleForInstanceGroupAndInstance(instanceGroupToBeScheduled, availableDatacenters);

                if (scheduledDc == Datacenter.NULL) {
                    interSchedulerResult.getFailedInstanceGroups().add(instanceGroupToBeScheduled);
                } else {
                    interSchedulerResult.addDcResult(instanceGroupToBeScheduled, scheduledDc);
                }
            }
        }

        return interSchedulerResult;
    }

    protected Datacenter scheduleForInstanceGroupAndInstance(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters) {
//        int dcStartIndex = random.nextInt(availableDatacenters.size());
        int hostSum = availableDatacenters.stream()
                .mapToInt(dc -> dc.getStatesManager().getHostNum())
                .sum();
        int hostStartIdInAll = random.nextInt(hostSum);
        int dcStartIndex = getDcIdByHostIdInAll(hostStartIdInAll, availableDatacenters);
        if (dcStartIndex == -1) {
            LOGGER.warn("return dcId = -1 in getDcIdByHostIdInAll");
            throw new RuntimeException("return dcId = -1 in getDcIdByHostIdInAll");
        }

        int i = 0;
        for (; i < availableDatacenters.size(); i++) {
            int dcIndex = (dcStartIndex + i) % availableDatacenters.size();
            Datacenter dcSelected = availableDatacenters.get(dcIndex);

            boolean isSuccessScheduled = scheduleHostInDcForInstanceGroup(instanceGroup, dcSelected);

            if (isSuccessScheduled) {
                return dcSelected;
            }
        }
        return Datacenter.NULL;
    }

    protected int getDcIdByHostIdInAll(int hostIdInAll, List<Datacenter> availableDatacenters) {
        for (int i = 0; i < availableDatacenters.size(); i++) {
            Datacenter datacenter = availableDatacenters.get(i);
            hostIdInAll -= datacenter.getStatesManager().getHostNum();
            if (hostIdInAll < 0) {
                return i;
            }
        }
        return -1;
    }

    private boolean scheduleHostInDcForInstanceGroup(InstanceGroup instanceGroup, Datacenter datacenter) {
        if (interScheduleSimpleStateMap.containsKey(datacenter)
                && interScheduleSimpleStateMap.get(datacenter) instanceof DetailedDcStateSimple detailedDcStateSimple) {
            Map<Instance, Integer> scheduleResult = new HashMap<>();

            for (Instance instance : instanceGroup.getInstances()) {
                int scheduledHostId = randomScheduleInstanceByDetailedDcStateSimple(instance, detailedDcStateSimple);

                if (scheduledHostId != -1) {
                    scheduleResult.put(instance, scheduledHostId);
                    detailedDcStateSimple.allocate(instance, scheduledHostId);
                } else {
                    break;
                }
            }

            if (scheduleResult.size() == instanceGroup.getInstances().size()) {
                recordScheduledResultInInstances(scheduleResult);
                return true;
            } else {
                return false;
            }
        } else {
            throw new IllegalStateException("InterSchedulerSimple.scheduleHostInDcForInstanceGroup: Invalid state of " + datacenter.getName());
        }
    }

    private int randomScheduleInstanceByDetailedDcStateSimple(Instance instance, DetailedDcStateSimple detailedDcStateSimple) {
        int hostNum = detailedDcStateSimple.getHostNum();
        int startIndex = random.nextInt(hostNum);

        for (int i = 0; i < hostNum; i++) {
            int index = (startIndex + i) % hostNum;
            HostState hostState = detailedDcStateSimple.getHostState(index);

            if (hostState.isSuitable(instance)) {
                traversalTime += i+1;
                return index;
            }
        }

        traversalTime += hostNum;
        return -1;
    }

    private void recordScheduledResultInInstances(Map<Instance, Integer> scheduleResult) {
        for (Map.Entry<Instance, Integer> scheduleResultEntry : scheduleResult.entrySet()) {
            Instance instance = scheduleResultEntry.getKey();
            int hostId = scheduleResultEntry.getValue();

            instance.setExpectedScheduleHostId(hostId);
        }
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
        this.decideReceiveGroupResultCostTime = 0.1;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
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

    @Override
    public void synBetweenDcState(List<Datacenter> datacenters) {
        for (Datacenter datacenter : datacenters) {
            if (!dcStateSynType.containsKey(datacenter)) {
                throw new IllegalStateException("InterSchedulerSimple.synBetweenDcState: There is not type of " + datacenter.getName());
            }

            String stateType = dcStateSynType.get(datacenter);
            interScheduleSimpleStateMap.put(datacenter, datacenter.getStatesManager().getStateByType(stateType));
        }
    }

    @Override
    public void addUserRequests(List<UserRequest> userRequests) {
        instanceGroupQueue.add(userRequests);
    }

    @Override
    public void addInstanceGroups(List<InstanceGroup> instanceGroups, boolean isRetry) {
        if (isRetry) {
            retryInstanceGroupQueue.add(instanceGroups);
        } else {
            instanceGroupQueue.add(instanceGroups);
        }
    }

    @Override
    public boolean isQueuesEmpty() {
        return instanceGroupQueue.size() == 0 && retryInstanceGroupQueue.size() == 0;
    }

    @Override
    public int getNewQueueSize() {
        return instanceGroupQueue.size();
    }

    @Override
    public int getRetryQueueSize() {
        return retryInstanceGroupQueue.size();
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
        // Filter based on access latency
        allDatacenters.removeIf(
                datacenter -> instanceGroup.getAccessLatency() <= networkTopology.getAccessLatency(instanceGroup.getUserRequest(), datacenter));
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
