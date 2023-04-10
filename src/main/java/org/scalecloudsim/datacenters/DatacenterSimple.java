package org.scalecloudsim.datacenters;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.cloudsimplus.network.topologies.NetworkTopology;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.Instances.InstanceGroup;
import org.scalecloudsim.Instances.UserRequest;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.statemanager.StateManager;
import org.scalecloudsim.statemanager.StateManagerSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DatacenterSimple extends CloudSimEntity implements Datacenter {
    public Logger LOGGER = LoggerFactory.getLogger(DatacenterSimple.class.getSimpleName());
    private Set<Integer> collaborationIds;
    private GroupQueue groupQueue;
    private InstanceQueue instanceQueue;
    @Getter
    private StateManager stateManager;
    @Getter
    private int hostNum;
    @Getter
    List<InnerScheduler> innerSchedulers;
    @Getter
    LoadBalance loadBalance;
    @Getter
    @Setter
    ResourceAllocateSelector resourceAllocateSelector;

    private Map<Integer, List<Instance>> innerSchedulerResult;

    private Map<InstanceGroup, Map<Datacenter, Integer>> instanceGroupSendResultMap;

    /**
     * Creates a new entity.
     *
     * @param simulation The CloudSimPlus instance that represents the simulation the Entity belongs to
     * @throws IllegalArgumentException when the entity name is invalid
     */
    public DatacenterSimple(@NonNull Simulation simulation) {
        super(simulation);
        this.collaborationIds = new HashSet<>();
        this.groupQueue = new GroupQueueFifo();
        this.instanceQueue = new InstanceQueueFifo();
        this.instanceGroupSendResultMap = new HashMap<>();
        this.innerSchedulerResult = new HashMap<>();
        this.resourceAllocateSelector = new ResourceAllocateSelectorSimple();
        this.resourceAllocateSelector.setDatacenter(this);
    }

    public DatacenterSimple(@NonNull Simulation simulation, int id) {
        this(simulation);
        this.setId(id);
    }

    public DatacenterSimple(@NonNull Simulation simulation, int id, int hostNum) {
        this(simulation);
        this.setId(id);
        this.stateManager = new StateManagerSimple(hostNum, simulation);
    }

    @Override
    public Datacenter setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
        stateManager.setDatacenter(this);
        return this;
    }

    @Override
    public Datacenter addCollaborationId(int collaborationId) {
        if (collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + this + ") already belongs to the collaboration " + collaborationId);
        } else {
            collaborationIds.add(collaborationId);
        }
        return this;
    }

    @Override
    public Datacenter removeCollaborationId(int collaborationId) {
        if (!collaborationIds.contains(collaborationId)) {
            LOGGER.warn("the datacenter(" + this + ") does not belong to the collaboration " + collaborationId + " to be removed");
        } else {
            collaborationIds.remove(collaborationId);
        }
        return this;
    }

    @Override
    public Set<Integer> getCollaborationIds() {
        return collaborationIds;
    }

    @Override
    public Datacenter setInnerSchedulers(List<InnerScheduler> innerSchedulers) {
        this.innerSchedulers = innerSchedulers;
        for (InnerScheduler innerScheduler : innerSchedulers) {
            innerScheduler.setDatacenter(this);
        }
        return this;
    }

    @Override
    public Datacenter setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
        loadBalance.setDatacenter(this);
        return this;
    }

    @Override
    protected void startInternal() {
        LOGGER.info("{}: {} is starting...", getSimulation().clockStr(), getName());
        sendNow(getSimulation().getCis(), CloudSimTag.DC_REGISTRATION_REQUEST, this);
    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case CloudSimTag.USER_REQUEST_SEND -> processUserRequestsSend(evt);
            case CloudSimTag.INTER_SCHEDULE -> processInterSchedule();
            case CloudSimTag.ASK_DC_REVIVE_GROUP -> processAskDcReviveGroup(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_ACCEPT, CloudSimTag.RESPOND_DC_REVIVE_GROUP_REJECT ->
                    processRespondDcReviveGroup(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_GIVE_UP -> processRespondDcReviveGroupGiveUp(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY -> processRespondDcReviveGroupEmploy(evt);
            case CloudSimTag.LOAD_BALANCE_SEND -> processLoadBalanceSend(evt);
            case CloudSimTag.INNER_SCHEDULE -> processInnerSchedule(evt);
            case CloudSimTag.SEND_INNER_SCHEDULE_RESULT -> processSendInnerScheduleResult(evt);
            case CloudSimTag.PRE_ALLOCATE_RESOURCE -> processPreAllocateResource(evt);
            case CloudSimTag.ALLOCATE_RESOURCE -> processAllocateResource(evt);
            case CloudSimTag.UPDATE_HOST_STATE -> processUpdateHostState(evt);
            default ->
                    LOGGER.warn("{}: {} received unknown event {}", getSimulation().clockStr(), getName(), evt.getTag());
        }
    }

    private void processUpdateHostState(SimEvent evt) {
        int hostId = (int) evt.getData();
        stateManager.updateHostState(hostId);
    }

    private void processAllocateResource(SimEvent evt) {
        if (evt.getData() instanceof Map) {
            Map<Integer, List<Instance>> allocateResult = (Map<Integer, List<Instance>>) evt.getData();
            for (Map.Entry<Integer, List<Instance>> entry : allocateResult.entrySet()) {
                int hostId = entry.getKey();
                List<Instance> instances = entry.getValue();
                for (Instance instance : instances) {
                    stateManager.allocateResource(hostId, instance);
                    List<Double> watchDelays = stateManager.getPartitionWatchDelay(hostId);
                    sendUpdateStateEvt(hostId, watchDelays);
                }
            }
        }
    }

    private void sendUpdateStateEvt(int hostId, List<Double> watchDelays) {
        for (Double watchDelay : watchDelays) {
            send(this, watchDelay, CloudSimTag.UPDATE_HOST_STATE, hostId);
        }
    }

    private void processPreAllocateResource(SimEvent evt) {
        Map<Integer, List<Instance>> allocateResult = resourceAllocateSelector.selectResourceAllocate(this.innerSchedulerResult);
        double costTime = 0.1;
        if (allocateResult.containsKey(-1)) {
            innerScheduleFailed(allocateResult.get(-1), 0.0);
            allocateResult.remove(-1);
        }
        send(this, costTime, CloudSimTag.ALLOCATE_RESOURCE, allocateResult);
    }

    private void processSendInnerScheduleResult(SimEvent evt) {
        if (evt.getData() instanceof Map innerSchedulerResultTmp) {
            Map<Integer, List<Instance>> result = (Map<Integer, List<Instance>>) innerSchedulerResultTmp;
            result.forEach((key, value) -> this.innerSchedulerResult.merge(key, value, (list1, list2) -> {
                List<Instance> mergedList = new ArrayList<>(list1);
                mergedList.addAll(list2);
                return mergedList;
            }));
            sendNow(this, CloudSimTag.PRE_ALLOCATE_RESOURCE, this);
        }
    }

    private void processInnerSchedule(SimEvent evt) {
        if (evt.getData() instanceof InnerScheduler innerScheduler) {
            Map<Integer, List<Instance>> scheduleResult = innerScheduler.schedule();
            double costTime = 0.2;//TODO 如何计算调度花费的时间
            if (scheduleResult.containsKey(-1)) {
                innerScheduleFailed(scheduleResult.get(-1), costTime);
                scheduleResult.remove(-1);
            }
            send(this, costTime, CloudSimTag.SEND_INNER_SCHEDULE_RESULT, scheduleResult);
            if (innerScheduler.getQueueSize() != 0) {
                send(this, costTime, CloudSimTag.INNER_SCHEDULE, innerScheduler);
            }
        }
    }

    private void innerScheduleFailed(List<Instance> instances, double delay) {
        for (Instance instance : instances) {
            instance.addRetryNum();
            if (!instance.isFailed()) {
                LOGGER.info("{}: {} failed to schedule instance{},it is retrying.", getSimulation().clockStr(), getName(), instance.getId());
                send(this, delay, CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY, instance);
            } else {
                LOGGER.warn("{}: {} failed to schedule instance{},The userRequest{} is failed.", getSimulation().clockStr(), getName(), instance.getId(), instance.getUserRequest().getId());
            }
        }
    }

    private void processLoadBalanceSend(SimEvent evt) {
        List<Instance> instances = instanceQueue.getBatchItem();
        if (instances.size() != 0) {
            List<InnerScheduler> sendedInnerScheduler = loadBalance.sendInstances(instances);
            double costTime = 0.1;//TODO 如何计算负载均衡花费的时间
            if (instanceQueue.size() > 0) {
                send(this, costTime, CloudSimTag.LOAD_BALANCE_SEND, null);
            }
            for (InnerScheduler innerScheduler : sendedInnerScheduler) {
                send(this, costTime, CloudSimTag.INNER_SCHEDULE, innerScheduler);
            }
        }
    }

    private void processRespondDcReviveGroupGiveUp(SimEvent evt) {
        //TODO 因为没有资源预留，所以不需要处理
    }

    private void processRespondDcReviveGroupEmploy(SimEvent evt) {
        if (evt.getData() instanceof InstanceGroup instanceGroup) {
            instanceQueue.add(instanceGroup);
            LOGGER.info("{}: {} receives {}'s respond to employ InstanceGroup{}.Now the size of instanceQueue is {}.",
                    getSimulation().clockStr(),
                    getName(),
                    ((Datacenter) evt.getSource()).getName(),
                    instanceGroup.getId(),
                    instanceQueue.size());
        } else if (evt.getData() instanceof Instance instance) {
            instanceQueue.add(instance);
            LOGGER.info("{}: {} receives {}'s respond to employ Instance{}.Now the size of instanceQueue is {}.",
                    getSimulation().clockStr(),
                    getName(),
                    ((Datacenter) evt.getSource()).getName(),
                    instance.getId(),
                    instanceQueue.size());
        }
        sendNow(this, CloudSimTag.LOAD_BALANCE_SEND);
    }

    private void processRespondDcReviveGroup(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroups) {
            for (Object instanceGroup : instanceGroups) {
                if (instanceGroup instanceof InstanceGroup) {
                    if (evt.getTag() == CloudSimTag.RESPOND_DC_REVIVE_GROUP_ACCEPT) {
                        instanceGroupSendResultMap.get(instanceGroup).put((Datacenter) evt.getSource(), 1);
                    } else if (evt.getTag() == CloudSimTag.RESPOND_DC_REVIVE_GROUP_REJECT) {
                        instanceGroupSendResultMap.get(instanceGroup).put((Datacenter) evt.getSource(), 0);
                    }
                    if (isAllSendResultReceived((InstanceGroup) instanceGroup)) {
                        Datacenter receiveDatacenter = decideReceiveDatacenter((InstanceGroup) instanceGroup);
                        LOGGER.info("{}: {} decides to schedule InstanceGroup{} to {} after receiving all respond.", getSimulation().clockStr(), getName(), ((InstanceGroup) instanceGroup).getId(), receiveDatacenter.getName());
                        double costTime = 0;//TODO 需要统计花费的时间
                        if (receiveDatacenter == null) {
                            interScheduleFail((InstanceGroup) instanceGroup, costTime);
                        } else {
                            //TODO 需要思考是否需要以List的形式回送，目前以单个的形式回送
                            respondAllReciveDatacenter((InstanceGroup) instanceGroup, receiveDatacenter, costTime);
                        }
                        instanceGroupSendResultMap.remove(instanceGroup);
                    }
                }
            }
        }
    }

    private void respondAllReciveDatacenter(InstanceGroup instanceGroup, Datacenter receiveDatacenter, double costTime) {
        for (Map.Entry<Datacenter, Integer> entry : instanceGroupSendResultMap.get(instanceGroup).entrySet()) {
            if (entry.getValue() == 1 && entry.getKey() != receiveDatacenter) {
                sendBetweenDc(entry.getKey(), costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP_GIVE_UP, instanceGroup);
            } else if (entry.getKey() == receiveDatacenter) {
                sendBetweenDc(entry.getKey(), costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY, instanceGroup);
            }
        }
    }

    private Datacenter decideReceiveDatacenter(InstanceGroup instanceGroup) {
        //TODO 决定接收的数据中心，目前先随机
        //得到所有数值为1表示接收的数据中心
        List<Datacenter> datacenters = instanceGroupSendResultMap.get(instanceGroup).entrySet().stream()
                .filter(entry -> entry.getValue() == 1)
                .map(Map.Entry::getKey)
                .toList();
        if (datacenters.size() == 0) {
            //表示调度失败
            return null;
        }
        return datacenters.get(new Random().nextInt(datacenters.size()));
    }

    private boolean isAllSendResultReceived(InstanceGroup instanceGroup) {
        for (Map.Entry<Datacenter, Integer> entry : instanceGroupSendResultMap.get(instanceGroup).entrySet()) {
            if (entry.getValue() == -1) {
                return false;
            }
        }
        return true;
    }

    private void processAskDcReviveGroup(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroups) {
            LOGGER.info("{}: {} received {} instance groups from {} to schedule.", getSimulation().clockStr(), getName(), instanceGroups.size(), evt.getSource().getName());
            Map<InstanceGroup, Boolean> reviveGroupResult = getReviveGroupResult((List<InstanceGroup>) instanceGroups);
            double costTime = instanceGroups.size() * 0.1;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
            respondAskDcReviveGroup(evt.getSource(), reviveGroupResult, costTime);
        }
    }

    private Map<InstanceGroup, Boolean> getReviveGroupResult(List<InstanceGroup> instanceGroups) {
        //TODO 怎么判断是否接收，如果接收了怎么进行资源预留
        Map<InstanceGroup, Boolean> result = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            result.put(instanceGroup, true);
        }
        return result;
    }

    private void respondAskDcReviveGroup(SimEntity dst, Map<InstanceGroup, Boolean> reviveGroupResult, double costTime) {
        List<InstanceGroup> acceptedGroups = new ArrayList<>();
        List<InstanceGroup> rejectedGroups = new ArrayList<>();
        for (Map.Entry<InstanceGroup, Boolean> entry : reviveGroupResult.entrySet()) {
            if (entry.getValue()) {
                acceptedGroups.add(entry.getKey());
            } else {
                rejectedGroups.add(entry.getKey());
            }
        }
        LOGGER.info("{}: {} is responding {} accepted groups and {} rejected groups to {}.", getSimulation().clockStr(), getName(), acceptedGroups.size(), rejectedGroups.size(), dst.getName());
        sendBetweenDc(dst, costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP_ACCEPT, acceptedGroups);
        sendBetweenDc(dst, costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP_REJECT, rejectedGroups);
    }

    private void processUserRequestsSend(final SimEvent evt) {
        if (evt.getData() instanceof List<?> userRequests) {
            for (Object userRequest : userRequests) {
                if (userRequest instanceof UserRequest) {
                    groupQueue.add((UserRequest) userRequest);
                }
            }
            LOGGER.info("{}: {} received {} user request.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), userRequests.size(), groupQueue.size());
        } else if (evt.getData() instanceof InstanceGroup) {
            groupQueue.add((InstanceGroup) evt.getData());
            LOGGER.info("{}: {} received an InstanceGroup.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), groupQueue.size());
        }
        sendNow(this, CloudSimTag.INTER_SCHEDULE);
    }

    private void processInterSchedule() {
        //得到本轮需要进行域间调度的亲和组
        List<InstanceGroup> instanceGroups = groupQueue.getBatchItem();
        LOGGER.info("{}: {} is processing inter schedule for {} instance groups.", getSimulation().clockStr(), getName(), instanceGroups.size());
        if (instanceGroups.size() == 0) {
            return;
        }
        //得到其他数据中心的基础信息和资源抽样信息
        List<Datacenter> allDatacenters = getSimulation().getCollaborationManager().getDatacenters(this);
        NetworkTopology networkTopology = getSimulation().getNetworkTopology();
        //根据接入时延和资源抽样信息得到每个亲和组可调度的数据中心
//        double start = System.currentTimeMillis();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = getAvaiableDatacenters(instanceGroup, allDatacenters, networkTopology);
            instanceGroupAvaiableDatacenters.put(instanceGroup, availableDatacenters);
        }
        interScheduleByNetworkTopology(instanceGroupAvaiableDatacenters, networkTopology);
//        double costTime = (System.currentTimeMillis() - start) / 10;//假设在集群调度器中的性能更强。只需要花费十分之一的时间
        double costTime = instanceGroups.size() * 0.1;//TODO 为了模拟没有随机性，先设置为每一个亲和组调度花费0.1ms
        LOGGER.info("{}: {} inter scheduling cost {} ms.", getSimulation().clockStr(), getName(), costTime);
        interScheduleByResult(instanceGroupAvaiableDatacenters, costTime);
        if (groupQueue.size() > 0) {
            send(this, costTime, CloudSimTag.INTER_SCHEDULE, null);
        }
        //根据网络拓扑中的时延和宽带情况对整个一批的进行排序
        //进行域间调度
        /*
         * 0.得到同一个协作区的所有数据中心
         * 1.根据接入时延要求得到可调度的数据中心
         * 2.根据个数据中心的资源抽样信息得到可调度的数据中心
         * 3.在可调度的数据中心中根据网络时延和宽带情况，每个个亲和组都可能会得到多个的调度方案
         * 4.将亲和组发送给调度方案中的各个数据中心进行询问
         * 4.如果没有可以的调度方案就当做失败将其返回给亲和组队列等待下次调度
         * 5.各个数据中心接收亲和组调度请求，并进行决策，决定是否接收该亲和组，如果决定接收就为其预留资源，并返回结果信息
         * 6.原数据中心接收发送出去的各个亲和组的调度结果，进行最终决策，为其决定指定的数据中心，并发送信息释放其他数据中心的资源
         * 7.各个数据中心如果接收到释放资源的消息，就释放资源，如果接收到确认亲和组放置的信息就将其实例放入到域内实例调度请求队列中
         */
    }

    //TODO 如果前一个亲和组被可能被分配给多个数据中心，那么后一个亲和组在分配的时候应该如何更新资源状态。目前是不考虑
    private List<Datacenter> getAvaiableDatacenters(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        //根据接入时延要求得到可调度的数据中心
        filterDatacentersByAccessLatency(instanceGroup, allDatacenters, networkTopology);
        //根据资源抽样信息得到可调度的数据中心
        filterDatacentersByResourceSample(instanceGroup, allDatacenters);
        //根据网络时延和宽带情况以及抽样信息得到最优的调度方案
        return allDatacenters;
    }

    private void filterDatacentersByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        allDatacenters.removeIf(datacenter -> instanceGroup.getAcessLatency() < networkTopology.getAcessLatency(this));
    }

    private void filterDatacentersByResourceSample(InstanceGroup instanceGroup, List<Datacenter> allDatacenters) {
        allDatacenters.removeIf(
                datacenter -> datacenter.getStateManager().getSimpleState().getCpuAvaiableSum() < instanceGroup.getCpuSum() ||
                        datacenter.getStateManager().getSimpleState().getRamAvaiableSum() < instanceGroup.getRamSum() ||
                        datacenter.getStateManager().getSimpleState().getStorageAvaiableSum() < instanceGroup.getStorageSum() ||
                        datacenter.getStateManager().getSimpleState().getBwAvaiableSum() < instanceGroup.getBwSum()
        );
        for (Datacenter datacenter : allDatacenters) {
            Map<Integer, Map<Integer, Integer>> instanceCpuRamNum = new HashMap<>();//记录一下所有Instance的cpu—ram的种类情况
            for (Instance instance : instanceGroup.getInstanceList()) {
                int allocateNum = instanceCpuRamNum.getOrDefault(instance.getCpu(), new HashMap<>()).getOrDefault(instance.getRam(), 0);
                if (datacenter.getStateManager().getSimpleState().getCpuRamSum(instance.getCpu(), instance.getRam()) - allocateNum <= 0) {
                    //如果该数据中心的资源不足以满足亲和组的资源需求，那么就将其从可调度的数据中心中移除
                    allDatacenters.remove(datacenter);
                    break;
                } else {
                    //如果该数据中心的资源可以满足亲和组的资源需求，那么就记录更新所有Instance的cpu—ram的种类情况
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

    //根据筛选情况进行调度
    private void interScheduleByResult(Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters, double costTime) {
        Map<Datacenter, List<InstanceGroup>> sendMap = new HashMap<>();
        for (Map.Entry<InstanceGroup, List<Datacenter>> entry : instanceGroupAvaiableDatacenters.entrySet()) {
            InstanceGroup instanceGroup = entry.getKey();
            List<Datacenter> datacenters = entry.getValue();
            if (datacenters.size() == 0) {
                //如果没有可调度的数据中心，那么就将其返回给亲和组队列等待下次调度
                interScheduleFail(instanceGroup, costTime);
            } else {
                //如果有可调度的数据中心，那么就将其发送给可调度的数据中心
                for (Datacenter datacenter : datacenters) {
                    if (sendMap.containsKey(datacenter)) {
                        sendMap.get(datacenter).add(instanceGroup);
                    } else {
                        List<InstanceGroup> instanceGroups = new ArrayList<>();
                        instanceGroups.add(instanceGroup);
                        sendMap.put(datacenter, instanceGroups);
                    }
                    //维护instanceGroupSendResultMap，以统计后续的返回结果
                    if (instanceGroupSendResultMap.containsKey(instanceGroup)) {
                        instanceGroupSendResultMap.get(instanceGroup).put(datacenter, -1);
                    } else {
                        Map<Datacenter, Integer> datacenterIntegerMap = new HashMap<>();
                        datacenterIntegerMap.put(datacenter, -1);
                        instanceGroupSendResultMap.put(instanceGroup, datacenterIntegerMap);
                    }
                }
            }
        }
        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : sendMap.entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();
            sendBetweenDc(datacenter, costTime, CloudSimTag.ASK_DC_REVIVE_GROUP, instanceGroups);
        }
    }

    private void interScheduleFail(InstanceGroup instanceGroup, double delay) {
        //如果重试次数增加了之后没有超过最大重试次数，那么就将其重新放入队列中等待下次调度
        instanceGroup.addRetryNum();
        if (!instanceGroup.isFailed()) {
            send(this, delay, CloudSimTag.USER_REQUEST_SEND, instanceGroup);
        }
    }

    @Override
    public int compareTo(SimEntity o) {
        return Comparator.comparing(SimEntity::getId).compare(this, o);
    }
}