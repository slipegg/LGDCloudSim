package org.scalecloudsim.datacenter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.scalecloudsim.innerscheduler.InnerScheduleResult;
import org.scalecloudsim.interscheduler.InterScheduler;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.request.InstanceGroup;
import org.scalecloudsim.request.InstanceGroupEdge;
import org.scalecloudsim.request.UserRequest;
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
    InterScheduler interScheduler;
    @Getter
    List<InnerScheduler> innerSchedulers;
    @Getter
    LoadBalance loadBalance;
    @Getter
    @Setter
    ResourceAllocateSelector resourceAllocateSelector;

    @Getter
    @Setter
    double unitCpuPrice;

    @Getter
    private double cpuCost;

    @Getter
    @Setter
    private double unitRamPrice;

    @Getter
    private double ramCost;

    @Getter
    @Setter
    private double unitStoragePrice;

    @Getter
    private double storageCost;

    @Getter
    @Setter
    private double unitBwPrice;

    private int usedCpuNum;

    @Getter
    @Setter
    private int cpuNumPerRack;

    @Getter
    @Setter
    private double unitRackPrice;

    @Getter
    private double bwCost;

    private List<InnerScheduleResult> innerSchedulerResults;

    private Map<InstanceGroup, Map<Datacenter, Integer>> instanceGroupSendResultMap;

    private boolean isGroupFilterDcBusy = false;

    private Map<InnerScheduler, Boolean> isInnerSchedulerBusy = new HashMap<>();

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
        this.innerSchedulerResults = new ArrayList<>();
        this.resourceAllocateSelector = new ResourceAllocateSelectorSimple();
        this.resourceAllocateSelector.setDatacenter(this);
        this.unitCpuPrice = 1.0;
        this.unitRamPrice = 1.0;
        this.unitStoragePrice = 1.0;
        this.unitBwPrice = 1.0;
        this.unitRackPrice = 100.0;
        this.cpuNumPerRack = 10;
        this.usedCpuNum = 0;
        this.cpuCost = 0.0;
        this.ramCost = 0.0;
        this.storageCost = 0.0;
        this.bwCost = 0.0;
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
    public Datacenter setInterScheduler(InterScheduler interScheduler) {
        this.interScheduler = interScheduler;
        interScheduler.setDatacenter(this);
        return this;
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
    public double getRackCost() {
        return Math.ceil((double) usedCpuNum / cpuNumPerRack) * unitRackPrice;
    }

    @Override
    public double getAllCost() {
        return cpuCost + ramCost + storageCost + bwCost + getRackCost();
    }

    @Override
    protected void startInternal() {
        LOGGER.info("{}: {} is starting...", getSimulation().clockStr(), getName());
        sendNow(getSimulation().getCis(), CloudSimTag.DC_REGISTRATION_REQUEST, this);
    }

    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            //TODO 要思考怎么反应调度时间影响，调度应该还是要分为开始调度和结束调度两个事件
            case CloudSimTag.USER_REQUEST_SEND -> processUserRequestsSend(evt);
            case CloudSimTag.GROUP_FILTER_DC_BEGIN -> processGroupFilterDcBegin();
            case CloudSimTag.GROUP_FILTER_DC_END -> processGroupFilterDcEnd(evt);
            case CloudSimTag.ASK_DC_REVIVE_GROUP -> processAskDcReviveGroup(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_ACCEPT, CloudSimTag.RESPOND_DC_REVIVE_GROUP_REJECT ->
                    processRespondDcReviveGroup(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_GIVE_UP -> processRespondDcReviveGroupGiveUp(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY -> processRespondDcReviveGroupEmploy(evt);
            case CloudSimTag.LOAD_BALANCE_SEND -> processLoadBalanceSend(evt);//负载均衡花费时间，不形成瓶颈
            case CloudSimTag.INNER_SCHEDULE_BEGIN -> processInnerScheduleBegin(evt);
            case CloudSimTag.INNER_SCHEDULE_END -> processInnerScheduleEnd(evt);
            case CloudSimTag.PRE_ALLOCATE_RESOURCE -> processPreAllocateResource(evt);
            case CloudSimTag.ALLOCATE_RESOURCE -> processAllocateResource(evt);
            case CloudSimTag.UPDATE_HOST_STATE -> processUpdateHostState(evt);
            case CloudSimTag.END_INSTANCE_RUN -> processEndInstanceRun(evt);
            default ->
                    LOGGER.warn("{}: {} received unknown event {}", getSimulation().clockStr(), getName(), evt.getTag());
        }
    }

    private void calculateCost(Instance instance) {
        if (instance.getStartTime() == -1 || instance.getFinishTime() == -1) {
            return;
        }
        cpuCost += instance.getCpu() * unitCpuPrice * (instance.getFinishTime() - instance.getStartTime());
        ramCost += instance.getRam() * unitRamPrice * (instance.getFinishTime() - instance.getStartTime());
        storageCost += instance.getStorage() * unitStoragePrice * (instance.getFinishTime() - instance.getStartTime());
        bwCost += instance.getBw() * unitBwPrice * (instance.getFinishTime() - instance.getStartTime());
        usedCpuNum += instance.getCpu();
    }

    private void processEndInstanceRun(SimEvent evt) {
        if (evt.getData() instanceof Instance instance) {
            if (instance.getState() != UserRequest.RUNNING) {
                return;
            }
            finishInstance(instance);
        }
    }

    private void finishInstance(Instance instance) {
        int hostId = instance.getHost();
        if (getSimulation().clock() - instance.getStartTime() >= instance.getLifeTime()) {
            instance.setState(UserRequest.SUCCESS);
            LOGGER.info("{}: {}'s Instance{} successfully completed running on host{} and resources have been released", getSimulation().clockStr(), getName(), instance.getId(), hostId);
        } else {
            instance.setState(UserRequest.FAILED);
            LOGGER.warn("{}: {}'s Instance{} is terminated prematurely on host{} and resources have been released", getSimulation().clockStr(), getName(), instance.getId(), hostId);
        }
        instance.setFinishTime(getSimulation().clock());
        stateManager.releaseResource(hostId, instance);
        List<Double> watchDelays = stateManager.getPartitionWatchDelay(hostId);
        sendUpdateStateEvt(hostId, watchDelays);
        getSimulation().getSqlRecord().recordInstanceFinishInfo(instance);
        calculateCost(instance);
        updateGroupAndUserRequestState(instance);
    }

    private void updateGroupAndUserRequestState(Instance instance) {
        InstanceGroup instanceGroup = instance.getInstanceGroup();
        if (instance.getState() == UserRequest.SUCCESS) {
            instanceGroup.addSuccessInstanceNum();
        }
        if (instanceGroup.getState() != UserRequest.SUCCESS) {
            return;
        } else {
            instanceGroup.setFinishTime(getSimulation().clock());
        }
        //instanceGroup的所有实例都已经成功运行完毕
        LOGGER.info("{}: {}'s InstanceGroup{} successfully completed running.", getSimulation().clockStr(), getName(), instanceGroup.getId());
        getSimulation().getSqlRecord().recordInstanceGroupFinishInfo(instanceGroup);

        UserRequest userRequest = instanceGroup.getUserRequest();
        //释放Bw资源
        List<InstanceGroup> dstInstanceGroups = userRequest.getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dstInstanceGroup : dstInstanceGroups) {
            if (dstInstanceGroup.getState() == UserRequest.SUCCESS) {
                double releaseBw = userRequest.getInstanceGroupGraph().getBw(instanceGroup, dstInstanceGroup);
                getSimulation().getNetworkTopology().releaseBw(instanceGroup.getReceiveDatacenter(), dstInstanceGroup.getReceiveDatacenter(), releaseBw);
            }
        }
        List<InstanceGroup> srcInstanceGroups = userRequest.getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup srcInstanceGroup : srcInstanceGroups) {
            if (srcInstanceGroup.getState() == UserRequest.SUCCESS) {
                double releaseBw = userRequest.getInstanceGroupGraph().getBw(srcInstanceGroup, instanceGroup);
                getSimulation().getNetworkTopology().releaseBw(srcInstanceGroup.getReceiveDatacenter(), instanceGroup.getReceiveDatacenter(), releaseBw);
            }
        }
        //如果InstanceGroup成功运行了就需要更新UserRequest状态信息
        userRequest.addSuccessGroupNum();
        if (userRequest.getState() == UserRequest.SUCCESS) {
            LOGGER.info("{}: {} successfully completed running.", getSimulation().clockStr(), getName());
            userRequest.setFinishTime(getSimulation().clock());
            getSimulation().getSqlRecord().recordUserRequestFinishInfo(userRequest);
        }
    }

    private void processUpdateHostState(SimEvent evt) {
        LOGGER.info("{}: {} is updating host state.", getSimulation().clockStr(), getName());
        int hostId = (int) evt.getData();
        stateManager.updateHostState(hostId);
    }

    private void processAllocateResource(SimEvent evt) {
        if (evt.getData() instanceof Map) {
            Map<Integer, List<Instance>> allocateResult = (Map<Integer, List<Instance>>) evt.getData();
            LOGGER.info("{}: {} is allocate resource for:{}.", getSimulation().clockStr(), getName(), allocateResult);
            List<Instance> failedInstances = null;
            for (Map.Entry<Integer, List<Instance>> entry : allocateResult.entrySet()) {
                int hostId = entry.getKey();
                List<Instance> instances = entry.getValue();
                for (Instance instance : instances) {
                    if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                        continue;
                    }
                    if (!stateManager.allocateResource(hostId, instance))//理论上到这里不会出现分配失败的情况
                    {
                        if (failedInstances == null) {
                            failedInstances = new ArrayList<>();
                        }
                        failedInstances.add(instance);
                    }
                    instance.setState(UserRequest.RUNNING);
                    instance.setHost(hostId);
                    instance.setStartTime(getSimulation().clock());
                    List<Double> watchDelays = stateManager.getPartitionWatchDelay(hostId);
                    sendUpdateStateEvt(hostId, watchDelays);
                    int lifeTime = instance.getLifeTime();
                    if (lifeTime > 0) {
                        send(this, lifeTime, CloudSimTag.END_INSTANCE_RUN, instance);
                    }
                    getSimulation().getSqlRecord().recordInstanceCreateInfo(instance);
                }
            }
            if (failedInstances != null) {
                innerScheduleFailed(failedInstances);
            }
        }
    }

    private void sendUpdateStateEvt(int hostId, List<Double> watchDelays) {
        for (Double watchDelay : watchDelays) {
            send(this, watchDelay, CloudSimTag.UPDATE_HOST_STATE, hostId);
        }
    }

    private void processPreAllocateResource(SimEvent evt) {
        LOGGER.info("{}: {}'s all innerScheduler result has been collected.it is dealing with scheduling conflicts...", getSimulation().clockStr(), getName());
        Map<Integer, List<Instance>> allocateResult = resourceAllocateSelector.selectResourceAllocate(this.innerSchedulerResults);
        this.innerSchedulerResults.clear();
        if (allocateResult.containsKey(-1)) {
            innerScheduleFailed(allocateResult.get(-1));
            allocateResult.remove(-1);
        }
        if (!allocateResult.isEmpty()) {
            send(this, 0, CloudSimTag.ALLOCATE_RESOURCE, allocateResult);
        }
    }

    private void processInnerScheduleEnd(SimEvent evt) {
        if (evt.getData() instanceof InnerScheduleResult innerSchedulerResult) {
            Map<Integer, List<Instance>> scheduleResult = innerSchedulerResult.getScheduleResult();
            InnerScheduler innerScheduler = innerSchedulerResult.getInnerScheduler();
            LOGGER.info("{}: {}'s {} ends scheduling {} instances", getSimulation().clockStr(), getName(), innerScheduler.getName(), scheduleResult.size());
            if (scheduleResult.containsKey(-1)) {
                innerScheduleFailed(scheduleResult.get(-1));
                scheduleResult.remove(-1);
            }
            innerSchedulerResults.add(innerSchedulerResult);
            send(this, 0, CloudSimTag.PRE_ALLOCATE_RESOURCE, null);
            if (innerScheduler.getQueueSize() != 0) {
                send(this, 0, CloudSimTag.INNER_SCHEDULE_BEGIN, innerScheduler);
            } else {
                isInnerSchedulerBusy.put(innerScheduler, false);
            }
        }
    }

    private void processInnerScheduleBegin(SimEvent evt) {
        if (evt.getData() instanceof InnerScheduler innerScheduler) {
            InnerScheduleResult innerScheduleResult = new InnerScheduleResult(innerScheduler);
            innerScheduleResult.setScheduleResult(innerScheduler.schedule());
            double costTime = innerScheduler.getScheduleCostTime();
            send(this, costTime, CloudSimTag.INNER_SCHEDULE_END, innerScheduleResult);
        }
    }

    private void innerScheduleFailed(List<Instance> instances) {
        for (Instance instance : instances) {
            instance.addRetryNum();
            if (instance.isFailed()) {
                UserRequest userRequest = instance.getUserRequest();
                userRequest.setFailReason("instance" + instance.getId() + " failed to schedule");
                send(getSimulation().getCis(), 0, CloudSimTag.USER_REQUEST_FAIL, userRequest);
            } else {
                LOGGER.info("{}: {} failed to schedule instance{},it is retrying.", getSimulation().clockStr(), getName(), instance.getId());
                send(this, 0, CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY, instance);
            }
        }
    }

    private void processLoadBalanceSend(SimEvent evt) {
        List<Instance> instances = instanceQueue.getBatchItem();
        if (instances.size() != 0) {
            List<InnerScheduler> sendedInnerScheduler = loadBalance.sendInstances(instances);
            if (instanceQueue.size() > 0) {
                send(this, 0, CloudSimTag.LOAD_BALANCE_SEND, null);
            }
            for (InnerScheduler innerScheduler : sendedInnerScheduler) {
                if (!isInnerSchedulerBusy.containsKey(innerScheduler) || !isInnerSchedulerBusy.get(innerScheduler)) {
                    send(this, 0, CloudSimTag.INNER_SCHEDULE_BEGIN, innerScheduler);
                    isInnerSchedulerBusy.put(innerScheduler, true);
                }
            }
        }
    }

    private void processRespondDcReviveGroupGiveUp(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroupsTmp) {
            List<InstanceGroup> instanceGroups = (List<InstanceGroup>) instanceGroupsTmp;
            instanceGroups.removeIf(instanceGroup -> instanceGroup.getUserRequest().getState() == UserRequest.FAILED);
            interScheduler.receiveNotEmployGroup((List<InstanceGroup>) instanceGroups);
        }
    }

    private void processRespondDcReviveGroupEmploy(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroupsTmp) {
            List<InstanceGroup> instanceGroups = (List<InstanceGroup>) instanceGroupsTmp;
            instanceGroups.removeIf(instanceGroup -> instanceGroup.getUserRequest().getState() == UserRequest.FAILED);
            instanceGroups.forEach(instanceGroup -> instanceGroup.setReceivedTime(getSimulation().clock()));
            interScheduler.receiveEmployGroup(instanceGroups);
            instanceQueue.add(instanceGroups);
            LOGGER.info("{}: {} receives {}'s respond to employ {} InstanceGroups.Now the size of instanceQueue is {}.",
                    getSimulation().clockStr(),
                    getName(),
                    evt.getSource().getName(),
                    instanceGroups.size(),
                    instanceQueue.size());
            getSimulation().getSqlRecord().recordInstanceGroupReceivedInfo(instanceGroups);
        } else if (evt.getData() instanceof Instance instance) {
            if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                return;
            }
            instanceQueue.add(instance);
            LOGGER.info("{}: {} receives {}'s respond to employ Instance{}.Now the size of instanceQueue is {}.",
                    getSimulation().clockStr(),
                    getName(),
                    evt.getSource().getName(),
                    instance.getId(),
                    instanceQueue.size());
        }
        sendNow(this, CloudSimTag.LOAD_BALANCE_SEND);
    }

    private void processRespondDcReviveGroup(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroups) {
            List<InstanceGroup> waitDecideInstanceGroups = null;
            for (Object instanceGroupTmp : instanceGroups) {
                if (instanceGroupTmp instanceof InstanceGroup instanceGroup) {
                    if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
                        instanceGroupSendResultMap.remove(instanceGroup);
                        continue;
                    }
                    if (evt.getTag() == CloudSimTag.RESPOND_DC_REVIVE_GROUP_ACCEPT) {
                        instanceGroupSendResultMap.get(instanceGroup).put((Datacenter) evt.getSource(), 1);
                    } else if (evt.getTag() == CloudSimTag.RESPOND_DC_REVIVE_GROUP_REJECT) {
                        instanceGroupSendResultMap.get(instanceGroup).put((Datacenter) evt.getSource(), 0);
                    }
                    if (isAllSendResultReceived(instanceGroup)) {
                        if (waitDecideInstanceGroups == null) {
                            waitDecideInstanceGroups = new ArrayList<>();
                        }
                        waitDecideInstanceGroups.add(instanceGroup);
                    }
                }
            }
            if (waitDecideInstanceGroups != null) {
                Map<InstanceGroup, Datacenter> decideResult = interScheduler.decideTargetDatacenter(instanceGroupSendResultMap, waitDecideInstanceGroups);
                double delay = interScheduler.getDecideTargetDatacenterCostTime();
                sendDecideResult(decideResult, delay);
                LOGGER.info("{}: {} decides to schedule {} InstanceGroup after receiving all respond.", getSimulation().clockStr(), getName(), decideResult.size());
            }
        }
    }

    private void sendDecideResult(Map<InstanceGroup, Datacenter> decideResult, double delay) {
        Map<Datacenter, Map<Integer, List<InstanceGroup>>> sendResult = new HashMap<>();
        for (Map.Entry<InstanceGroup, Datacenter> entry : decideResult.entrySet()) {
            InstanceGroup instanceGroup = entry.getKey();
            Datacenter receiveDatacenter = entry.getValue();
            if (receiveDatacenter == null) {
                interScheduleFail(instanceGroup);
            } else {
                instanceGroup.setReceiveDatacenter(receiveDatacenter);
                instanceGroup.setState(UserRequest.SCHEDULING);
                allocateBwForGroup(instanceGroup, receiveDatacenter);
                //整合发送的结果
                for (Datacenter datacenter : instanceGroupSendResultMap.get(instanceGroup).keySet()) {
                    if (!sendResult.containsKey(datacenter)) {
                        sendResult.put(datacenter, new HashMap<>());
                    }
                    if (datacenter == receiveDatacenter) {
                        if (!sendResult.get(datacenter).containsKey((Integer) 1)) {
                            sendResult.get(datacenter).put(1, new ArrayList<>());
                        }
                        sendResult.get(datacenter).get(1).add(instanceGroup);
                    } else {
                        if (!sendResult.get(datacenter).containsKey((Integer) 0)) {
                            sendResult.get(datacenter).put(0, new ArrayList<>());
                        }
                        sendResult.get(datacenter).get(0).add(instanceGroup);
                    }
                }
            }
            instanceGroupSendResultMap.remove(instanceGroup);
        }
        for (Map.Entry<Datacenter, Map<Integer, List<InstanceGroup>>> entry : sendResult.entrySet()) {
            Datacenter datacenter = entry.getKey();
            Map<Integer, List<InstanceGroup>> result = entry.getValue();
            for (Map.Entry<Integer, List<InstanceGroup>> entry1 : result.entrySet()) {
                int isEmploy = entry1.getKey();
                List<InstanceGroup> instanceGroups = entry1.getValue();
                if (isEmploy == 1) {
                    send(datacenter, delay, CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY, instanceGroups);
                } else {
                    send(datacenter, delay, CloudSimTag.RESPOND_DC_REVIVE_GROUP_GIVE_UP, instanceGroups);
                }
            }
        }
    }

    private void allocateBwForGroup(InstanceGroup instanceGroup, Datacenter receiveDatacenter) {
        UserRequest userRequest = instanceGroup.getUserRequest();
        List<InstanceGroup> dstInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dst : dstInstanceGroups) {
            if (dst.getReceiveDatacenter() != null) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(instanceGroup, dst);
                getSimulation().getNetworkTopology().allocateBw(receiveDatacenter, dst.getReceiveDatacenter(), edge.getRequiredBw());
                userRequest.addAllocatedEdge(edge);
            }
        }
        List<InstanceGroup> srcInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup src : srcInstanceGroups) {
            if (src.getReceiveDatacenter() != null) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(src, instanceGroup);
                getSimulation().getNetworkTopology().allocateBw(src.getReceiveDatacenter(), receiveDatacenter, edge.getRequiredBw());
                userRequest.addAllocatedEdge(edge);
            }
        }
    }


    //TODO 可以优化一下速度
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
            Map<InstanceGroup, Boolean> reviveGroupResult = interScheduler.decideReciveGroupResult((List<InstanceGroup>) instanceGroups);
            double costTime = interScheduler.getDecideReciveGroupResultCostTime();
            respondAskDcReviveGroup(evt.getSource(), reviveGroupResult, costTime);
        }
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
        if (acceptedGroups.size() > 0) {
            sendBetweenDc(dst, costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP_ACCEPT, acceptedGroups);
        }
        if (rejectedGroups.size() > 0) {
            sendBetweenDc(dst, costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP_REJECT, rejectedGroups);
        }
    }

    private void processUserRequestsSend(final SimEvent evt) {
        if (evt.getData() instanceof List<?> userRequests) {
            for (Object userRequestTmp : userRequests) {
                if (userRequestTmp instanceof UserRequest userRequest) {
                    groupQueue.add(userRequest);
                }
            }
            LOGGER.info("{}: {} received {} user request.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), userRequests.size(), groupQueue.size());
        } else if (evt.getData() instanceof InstanceGroup instanceGroup) {
            groupQueue.add(instanceGroup);
            LOGGER.info("{}: {} received an InstanceGroup.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), groupQueue.size());
        }
        if (!isGroupFilterDcBusy) {
            sendNow(this, CloudSimTag.GROUP_FILTER_DC_BEGIN);
            isGroupFilterDcBusy = true;//放在这里可以防止同一时间多次触发
        }
    }

    private void processGroupFilterDcBegin() {
        //得到本轮需要进行域间调度的亲和组
        List<InstanceGroup> instanceGroups = groupQueue.getBatchItem();
        LOGGER.info("{}: {} starts finding available Datacenters for {} instance groups.", getSimulation().clockStr(), getName(), instanceGroups.size());
        if (instanceGroups.size() == 0) {
            return;
        }
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters = interScheduler.filterSuitableDatacenter(instanceGroups);
        double filterSuitableDatacenterCostTime = interScheduler.getFilterSuitableDatacenterCostTime();
        send(this, filterSuitableDatacenterCostTime, CloudSimTag.GROUP_FILTER_DC_END, instanceGroupAvaiableDatacenters);
    }

    private void processGroupFilterDcEnd(final SimEvent evt) {
        if (evt.getData() instanceof Map<?, ?> instanceGroupAvaiableDatacentersTmp) {
            Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters = (Map<InstanceGroup, List<Datacenter>>) instanceGroupAvaiableDatacentersTmp;
            LOGGER.info("{}: {} ends finding available Datacenters for {} instance groups.", getSimulation().clockStr(), getName(), instanceGroupAvaiableDatacenters.size());
            interScheduleByResult(instanceGroupAvaiableDatacenters);
            if (groupQueue.size() > 0) {
                send(this, 0, CloudSimTag.GROUP_FILTER_DC_BEGIN, null);
            } else {
                isGroupFilterDcBusy = false;
            }
        }
    }

    //根据筛选情况进行调度
    private void interScheduleByResult(Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters) {
        Map<Datacenter, List<InstanceGroup>> sendMap = new HashMap<>();
        for (Map.Entry<InstanceGroup, List<Datacenter>> entry : instanceGroupAvaiableDatacenters.entrySet()) {
            InstanceGroup instanceGroup = entry.getKey();
            List<Datacenter> datacenters = entry.getValue();
            if (datacenters.size() == 0) {
                //如果没有可调度的数据中心，那么就要么再次尝试要么设置为失败
                interScheduleFail(instanceGroup);
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
        //向每个dc以list的形式发送instanceGroups
        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : sendMap.entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();
            sendBetweenDc(datacenter, 0, CloudSimTag.ASK_DC_REVIVE_GROUP, instanceGroups);
        }
    }

    private void interScheduleFail(InstanceGroup instanceGroup) {
        //如果重试次数增加了之后没有超过最大重试次数，那么就将其重新放入队列中等待下次调度
        instanceGroup.addRetryNum();
        if (!instanceGroup.isFailed()) {
            send(this, 0, CloudSimTag.USER_REQUEST_SEND, instanceGroup);
        } else {
            instanceGroup.getUserRequest().setFailReason("InstanceGroup" + instanceGroup.getId() + " failed to be scheduled.");
            send(getSimulation().getCis(), 0, CloudSimTag.USER_REQUEST_FAIL, instanceGroup.getUserRequest());
        }
    }

    @Override
    public int compareTo(SimEntity o) {
        return Comparator.comparing(SimEntity::getId).compare(this, o);
    }
}
