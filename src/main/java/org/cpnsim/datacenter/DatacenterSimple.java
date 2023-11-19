package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.cloudsimplus.core.CloudSimEntity;
import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.SimEntity;
import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.core.events.SimEvent;
import org.cpnsim.innerscheduler.InnerSchedulerResult;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.interscheduler.InterSchedulerResult;
import org.cpnsim.interscheduler.InterSchedulerSimple;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupEdge;
import org.cpnsim.request.UserRequest;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.statemanager.StatesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * An interface to be implemented by each class that represents a datacenter.
 * See {@link DatacenterSimple} for an example of how to implement this interface.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class DatacenterSimple extends CloudSimEntity implements Datacenter {
    /**
     * the Logger.
     **/
    public Logger LOGGER = LoggerFactory.getLogger(DatacenterSimple.class.getSimpleName());

    /**
     * See {@link GroupQueue}.
     */
    private GroupQueue groupQueue;

    /**
     * See {@link InstanceQueue}.
     */
    @Getter
    private InstanceQueue instanceQueue;

    /**
     * See {@link StatesManager}.
     */
    @Getter
    private StatesManager statesManager;

    /**
     * Get the collaborationIds to which the datacenter belongs.
     */
    @Getter
    private Set<Integer> collaborationIds;

    /**
     * See {@link InterScheduler}.
     */
    @Getter
    private InterScheduler interScheduler;

    /**
     * See {@link InnerScheduler}.
     */
    @Getter
    private List<InnerScheduler> innerSchedulers;

    /**
     * See {@link LoadBalance}.
     */
    @Getter
    private LoadBalance loadBalance;

    /**
     * See {@link ResourceAllocateSelector}.
     */
    @Getter
    private ResourceAllocateSelector resourceAllocateSelector;

    /**
     * The unit price of cpu resources,See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    double unitCpuPrice;

    /**
     * All cpu expenses, See {@link DatacenterPrice}.
     */
    @Getter
    private double cpuCost;

    /**
     * The unit price of ram resources,See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double unitRamPrice;

    /**
     * All ram expenses, See {@link DatacenterPrice}.
     */
    @Getter
    private double ramCost;

    /**
     * The unit price of storage resources,See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double unitStoragePrice;

    /**
     * All storage expenses, See {@link DatacenterPrice}.
     */
    @Getter
    private double storageCost;

    /**
     * The unit price of bw resources,See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double unitBwPrice;

    /**
     * All bw expenses, See {@link DatacenterPrice}.
     **/
    @Getter
    private double bwCost;

    /**
     * Number of CPUs in a rack.
     **/
    //TODO 需要删除这个概念
    @Getter
    @Setter
    private int cpuNumPerRack;

    /**
     * Rental price per rack.
     **/
    @Getter
    @Setter
    private double unitRackPrice;

    @Getter
    @Setter
    private String bwBillingType = "fixed";

    @Getter
    @Setter
    private double bwUtilization;

    @Getter
    private double TCOEnergy;

    @Getter
    private double TCORack;

    @Setter
    private boolean centralizedInterSchedule;

    /**
     * The InnerScheduleResult List.
     **/
    private List<InnerSchedulerResult> innerSchedulerResults;

    /**
     * The instanceGroup SendResult Map.It is used for inter scheduler.
     **/
    private Map<InstanceGroup, Map<Datacenter, Double>> instanceGroupSendResultMap;

    /**
     * Whether the interScheduler is busy.
     **/
    private boolean isGroupFilterDcBusy = false;

    /**
     * Whether the innerScheduler is busy.
     **/
    private Map<InnerScheduler, Boolean> isInnerSchedulerBusy = new HashMap<>();

    public DatacenterSimple(@NonNull Simulation simulation) {
        super(simulation);
        this.collaborationIds = new HashSet<>();
        this.groupQueue = new GroupQueueFifo();
        this.instanceQueue = new InstanceQueueFifo();
        this.instanceGroupSendResultMap = new HashMap<>();
        this.innerSchedulerResults = new ArrayList<>();
        this.unitCpuPrice = 1.0;
        this.unitRamPrice = 1.0;
        this.unitStoragePrice = 1.0;
        this.unitBwPrice = 1.0;
        this.unitRackPrice = 100.0;
        this.cpuNumPerRack = 10;
        this.cpuCost = 0.0;
        this.ramCost = 0.0;
        this.storageCost = 0.0;
        this.bwCost = 0.0;
    }

    public DatacenterSimple(@NonNull Simulation simulation, int id) {
        this(simulation);
        this.setId(id);
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
    public Datacenter setResourceAllocateSelector(ResourceAllocateSelector resourceAllocateSelector) {
        this.resourceAllocateSelector = resourceAllocateSelector;
        resourceAllocateSelector.setDatacenter(this);
        return this;
    }

    @Override
    public Datacenter setStatesManager(StatesManager statesManager) {
        this.statesManager = statesManager;
        statesManager.setDatacenter(this);
        return this;
    }

    @Override
    public double getRackCost() {
        int maxPowerOnHostNum = statesManager.getDatacenterPowerOnRecord().getMaxHostNum();
        return maxPowerOnHostNum * unitRackPrice;
    }

    @Override
    public double getResourceCost() {
        return cpuCost + ramCost + storageCost + bwCost;
    }

    @Override
    public double getAllCost() {
        return getResourceCost() + getRackCost();
    }

    /**
     * When a datacenter is created, it will send a registration request to the CIS.
     */
    @Override
    protected void startInternal() {
        LOGGER.info("{}: {} is starting...", getSimulation().clockStr(), getName());
        sendWithoutNetwork(getSimulation().getCis(), 0, CloudSimTag.DC_REGISTRATION_REQUEST, this);
        if (statesManager.isSynCostTime()) {
            send(this, statesManager.getNextSynDelay(), CloudSimTag.SYN_STATE_IN_DC, null);
        }

        if (interScheduler != null) {
            Map<Double, List<SimEntity>> initSynStateBetweenDcTargets = new HashMap<>();
            for (Map.Entry<Datacenter, Double> synDcGap : interScheduler.getDcStateSynInterval().entrySet()) {
                if (synDcGap.getValue() != 0) {
                    initSynStateBetweenDcTargets.putIfAbsent(synDcGap.getValue(), new ArrayList<>());
                    initSynStateBetweenDcTargets.get(synDcGap.getValue()).add(synDcGap.getKey());
                }
            }
            if (!initSynStateBetweenDcTargets.isEmpty()) {
                for (Map.Entry<Double, List<SimEntity>> entry : initSynStateBetweenDcTargets.entrySet()) {
                    sendWithoutNetwork(this, 0, CloudSimTag.SYN_STATE_BETWEEN_DC, entry.getValue());
                }
            }
        }
    }

    /**
     * The events that the datacenter needs to process.
     */
    @Override
    public void processEvent(SimEvent evt) {
        switch (evt.getTag()) {
            case CloudSimTag.SYN_STATE_IN_DC -> processSynStateInDc();
            case CloudSimTag.SYN_STATE_BETWEEN_DC -> processSynStateBetweenDc(evt);
            case CloudSimTag.USER_REQUEST_SEND, CloudSimTag.SCHEDULE_TO_DC_AND_FORWARD -> processUserRequestsSend(evt);
            case CloudSimTag.GROUP_FILTER_DC_BEGIN -> processGroupFilterDcBegin();
            case CloudSimTag.ASK_SIMPLE_STATE -> processAskSimpleState(evt);
            case CloudSimTag.RESPOND_SIMPLE_STATE -> processRespondSimpleState(evt);
            case CloudSimTag.GROUP_FILTER_DC_END -> processGroupFilterDcEnd(evt);
            case CloudSimTag.ASK_DC_REVIVE_GROUP -> processAskDcReviveGroup(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP -> processRespondDcReviveGroup(evt);
            case CloudSimTag.RESPOND_DC_REVIVE_GROUP_GIVE_UP -> processRespondDcReviveGroupGiveUp(evt);
            case CloudSimTag.SCHEDULE_TO_DC_NO_FORWARD -> processScheduleToDcNoForward(evt);
            case CloudSimTag.SCHEDULE_TO_DC_HOST -> processScheduleToDcHost(evt);
            case CloudSimTag.SCHEDULE_TO_DC_HOST_OK, CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED ->
                    processScheduleToDcHostResponse(evt);
            case CloudSimTag.LOAD_BALANCE_SEND -> processLoadBalanceSend(evt);//负载均衡花费时间，不形成瓶颈
            case CloudSimTag.INNER_SCHEDULE_BEGIN -> processInnerScheduleBegin(evt);
            case CloudSimTag.INNER_SCHEDULE_END -> processInnerScheduleEnd(evt);
            case CloudSimTag.PRE_ALLOCATE_RESOURCE -> processPreAllocateResource(evt);
            case CloudSimTag.END_INSTANCE_RUN -> processEndInstanceRun(evt);
            default ->
                    LOGGER.warn("{}: {} received unknown event {}", getSimulation().clockStr(), getName(), evt.getTag());
        }
    }

    //需要保证传递过来的都是同一个同步时间的dc,
    private void processSynStateBetweenDc(SimEvent evt) {
        if (evt.getData() instanceof List<?> synTargets) {
            if (!synTargets.isEmpty() && synTargets.get(0) instanceof Datacenter) {
                List<Datacenter> datacenters = (List<Datacenter>) synTargets;
                interScheduler.synBetweenDcState(datacenters);
                //TODO 需要考虑中途同步时间是否会变，会变的话需要检查，如果一直都不会变，就不改了
                sendWithoutNetwork(this, interScheduler.getDcStateSynInterval().get(datacenters.get(0)), CloudSimTag.SYN_STATE_BETWEEN_DC, datacenters);
            }
        }
    }

    /**
     * Calling {@link StatesManager#synAllState()} to synchronize the state of the datacenter.
     * And send a {@link CloudSimTag#SYN_STATE_IN_DC} event to itself after smallSynGap.
     */
    private void processSynStateInDc() {
        statesManager.synAllState();
        if (statesManager.isSynCostTime()) {
            send(this, statesManager.getNextSynDelay(), CloudSimTag.SYN_STATE_IN_DC, null);
        }
    }

    private void calculateCost(Instance instance) {
        if (instance.getStartTime() == -1 || instance.getFinishTime() == -1) {
            return;
        }
        double lifeTimeSec = (instance.getFinishTime() - instance.getStartTime()) / 1000.0;
        cpuCost += instance.getCpu() * unitCpuPrice * lifeTimeSec;
        ramCost += instance.getRam() * unitRamPrice * lifeTimeSec;
        storageCost += instance.getStorage() * unitStoragePrice * lifeTimeSec;
        bwCost += calculateInstanceBwCost(instance);
    }

    private double calculateInstanceBwCost(Instance instance) {
        double lifeTimeSec = (instance.getFinishTime() - instance.getStartTime()) / 1000.0;
        if (bwBillingType.equals("used")) {
            return (instance.getBw() * lifeTimeSec) / 8 / 1024 * bwUtilization * unitBwPrice;
        } else {
            return instance.getBw() * unitBwPrice * lifeTimeSec;
        }
    }

    /**
     * Handling events after instance run ends.
     *
     * @param evt The data of the event is a list of instances.
     */
    private void processEndInstanceRun(SimEvent evt) {
        if (evt.getData() instanceof List<?> list) {
            if (list.size() > 0 && list.get(0) instanceof Instance) {
//                LOGGER.info("{}: {} received {} instances to finish", getSimulation().clockStr(), getName(), list.size());
                for (Instance instance : (List<Instance>) list) {
                    finishInstance(instance);
                }
                getSimulation().getSqlRecord().recordInstancesFinishInfo((List<Instance>) list);
            }
        }
    }

    private void finishInstance(Instance instance) {
        int hostId = instance.getHost();
        if (getSimulation().clock() - instance.getStartTime() >= instance.getLifeTime() - 0.01 && instance.getLifeTime() != -1) {
            instance.setState(UserRequest.SUCCESS);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}: {}'s Instance{} successfully completed running on host{} and resources have been released", getSimulation().clockStr(), getName(), instance.getId(), hostId);
            }
        } else {
            instance.setState(UserRequest.FAILED);
            LOGGER.warn("{}: {}'s Instance{} is terminated prematurely on host{} and resources have been released", getSimulation().clockStr(), getName(), instance.getId(), hostId);
        }
        instance.setFinishTime(getSimulation().clock());
        statesManager.release(hostId, instance);
        updateTCO();
//        getSimulation().getSqlRecord().recordInstanceFinishInfo(instance);
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

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}: {}'s InstanceGroup{} successfully completed running.", getSimulation().clockStr(), getName(), instanceGroup.getId());
        }
        getSimulation().getSqlRecord().recordInstanceGroupFinishInfo(instanceGroup);

        UserRequest userRequest = instanceGroup.getUserRequest();
        //释放Bw资源

        List<InstanceGroup> dstInstanceGroups = userRequest.getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dstInstanceGroup : dstInstanceGroups) {
            if (dstInstanceGroup.getState() == UserRequest.SUCCESS) {
                double releaseBw = userRequest.getInstanceGroupGraph().getBw(instanceGroup, dstInstanceGroup);
                getSimulation().getNetworkTopology().releaseBw(instanceGroup.getReceiveDatacenter(), dstInstanceGroup.getReceiveDatacenter(), releaseBw);
                getSimulation().getSqlRecord().recordInstanceGroupGraphReleaseInfo(instance.getInstanceGroup().getId(), dstInstanceGroup.getId(), getSimulation().clock());
            }
        }
        List<InstanceGroup> srcInstanceGroups = userRequest.getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup srcInstanceGroup : srcInstanceGroups) {
            if (srcInstanceGroup.getState() == UserRequest.SUCCESS) {
                double releaseBw = userRequest.getInstanceGroupGraph().getBw(srcInstanceGroup, instanceGroup);
                getSimulation().getNetworkTopology().releaseBw(srcInstanceGroup.getReceiveDatacenter(), instanceGroup.getReceiveDatacenter(), releaseBw);
                getSimulation().getSqlRecord().recordInstanceGroupGraphReleaseInfo(srcInstanceGroup.getId(), instance.getInstanceGroup().getId(), getSimulation().clock());
            }
        }

        //如果InstanceGroup成功运行了就需要更新UserRequest状态信息
        userRequest.addSuccessGroupNum();
        if (userRequest.getState() == UserRequest.SUCCESS) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}: userRequest{} successfully completed running.", getSimulation().clockStr(), getName());
            }
            userRequest.setFinishTime(getSimulation().clock());
            getSimulation().getSqlRecord().recordUserRequestFinishInfo(userRequest);
        }
    }

    /**
     * Place instances on the host based on the results of the {@link InnerScheduler} and the strategy of the {@link ResourceAllocateSelector}.
     *
     * @param evt
     */
    private void processPreAllocateResource(SimEvent evt) {
        LOGGER.info("{}: {}'s all innerScheduler results have been collected.it is dealing with scheduling conflicts...", getSimulation().clockStr(), getName());
        ResourceAllocateResult allocateResult = resourceAllocateSelector.selectResourceAllocate(this.innerSchedulerResults);

        Map<InnerScheduler, List<Instance>> failedAllocatedRes = allocateResource(allocateResult.getSuccessRes());

        allocateResult.getFailRes().putAll(failedAllocatedRes);

        for (Map.Entry<InnerScheduler, List<Instance>> entry : allocateResult.getFailRes().entrySet()) {
            InnerScheduler innerScheduler = entry.getKey();
            List<Instance> failedInstances = entry.getValue();

            if (!failedInstances.isEmpty()) {
                innerScheduleFailed(failedInstances, innerScheduler, true);
            }
        }

        for (InnerSchedulerResult innerSchedulerResult : this.innerSchedulerResults) {
            InnerScheduler innerScheduler = innerSchedulerResult.getInnerScheduler();
            startInnerScheduling(innerScheduler);
        }

        this.innerSchedulerResults.clear();
    }

    private Map<InnerScheduler, List<Instance>> allocateResource(Map<InnerScheduler, List<Instance>> allocatedResult) {
        LOGGER.info("{}: {} is allocate resource.", getSimulation().clockStr(), getName());
        Map<Integer, List<Instance>> successAllocatedInstances = new HashMap<>();
        Map<InnerScheduler, List<Instance>> failedInstances = new HashMap<>();

        for (Map.Entry<InnerScheduler, List<Instance>> entry : allocatedResult.entrySet()) {
            InnerScheduler innerScheduler = entry.getKey();
            List<Instance> instances = entry.getValue();
            for (Instance instance : instances) {
                if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                    continue;
                }

                int allocatedHostId = instance.getExpectedScheduleHostId();
                if (!statesManager.allocate(allocatedHostId, instance))//理论上到这里不会出现分配失败的情况
                {
                    LOGGER.warn("{}: {}'s Instance{} failed to allocate resources on host{} after processPreAllocateResource", getSimulation().clockStr(), getName(), instance.getId(), instance.getExpectedScheduleHostId());
                    failedInstances.putIfAbsent(innerScheduler, new ArrayList<>());
                    failedInstances.get(innerScheduler).add(instance);
                }

                updateAfterInstanceAllocated(instance);

                int lifeTime = instance.getLifeTime();
                successAllocatedInstances.putIfAbsent(lifeTime, new ArrayList<>());
                successAllocatedInstances.get(lifeTime).add(instance);
            }
        }

        getSimulation().getSqlRecord().recordInstancesCreateInfo(successAllocatedInstances);
        sendFinishInstanceRunEvt(successAllocatedInstances);

        return failedInstances;
    }

    private void sendFinishInstanceRunEvt(Map<Integer, List<Instance>> finishInstances) {
        for (Map.Entry<Integer, List<Instance>> entry : finishInstances.entrySet()) {
            int lifeTime = entry.getKey();
            List<Instance> instances = entry.getValue();

            if (lifeTime > 0) {
                send(this, lifeTime, CloudSimTag.END_INSTANCE_RUN, instances);
            }
        }
    }

    /**
     * Publish the results of the {@link InnerScheduler}
     *
     * @param evt the data of the event is {@link InnerSchedulerResult}
     */
    private void processInnerScheduleEnd(SimEvent evt) {
        if (evt.getData() instanceof InnerSchedulerResult innerSchedulerResult) {
            InnerScheduler innerScheduler = innerSchedulerResult.getInnerScheduler();

            LOGGER.info("{}: {}'s {} ends scheduling instances.", getSimulation().clockStr(), getName(), innerScheduler.getName());

            if (!statesManager.isInLatestSmallSynGap(innerSchedulerResult.getScheduleTime())) {//把同步时对这一调度的记录补回来
                statesManager.revertHostState(innerSchedulerResult);
            }

            if (!innerSchedulerResult.isFailedInstancesEmpty()) {
                innerScheduleFailed(innerSchedulerResult.getFailedInstances(), innerScheduler, false);
            }
            innerSchedulerResults.add(innerSchedulerResult);
            if (!innerSchedulerResult.isScheduledInstancesEmpty()) {
                send(this, 0, CloudSimTag.PRE_ALLOCATE_RESOURCE, null);
            } else {
                isInnerSchedulerBusy.put(innerScheduler, false);
            }
//            if (innerScheduler.getNewInstanceQueueSize() != 0) {
//                send(this, 0, CloudSimTag.INNER_SCHEDULE_BEGIN, innerScheduler);
//            } else {
//                isInnerSchedulerBusy.put(innerScheduler, false);
//            }
        }
    }

    private void startInnerScheduling(InnerScheduler innerScheduler) {
        if (innerScheduler.isQueuesEmpty()) {
            isInnerSchedulerBusy.put(innerScheduler, false);
        } else if (!innerScheduler.isQueuesEmpty()) {
            send(this, 0, CloudSimTag.INNER_SCHEDULE_BEGIN, innerScheduler);
        }
    }

    /**
     * Call the {@link InnerScheduler} to allocate {@link Instance} to various hosts.
     * It will send INNER_SCHEDULE_END after {@link InnerScheduler#getScheduleCostTime()}
     *
     * @param evt
     */
    private void processInnerScheduleBegin(SimEvent evt) {
        if (evt.getData() instanceof InnerScheduler innerScheduler) {
            InnerSchedulerResult innerScheduleResult = innerScheduler.schedule();

            double costTime = innerScheduler.getScheduleCostTime();

            LOGGER.info("{}: {}'s {} starts scheduling {} instances,cost {} ms", getSimulation().clockStr(), this.getName(), innerScheduler.getName(), innerScheduleResult.getInstanceNum(), costTime);

            send(this, costTime, CloudSimTag.INNER_SCHEDULE_END, innerScheduleResult);
        }
    }

    private void innerScheduleFailed(List<Instance> instances, InnerScheduler innerScheduler, boolean isNeedRevertSelfHostState) {
        Iterator<Instance> instanceIterator = instances.iterator();
        Set<UserRequest> failedUserRequests = new HashSet<>();

        while (instanceIterator.hasNext()) {
            Instance instance = instanceIterator.next();
            if (instance.getExpectedScheduleHostId() != -1) {
                instance.addRetryHostId(instance.getExpectedScheduleHostId());
                instance.setExpectedScheduleHostId(-1);
            }

            instance.addRetryNum();
            if (instance.isFailed()) {
                UserRequest userRequest = instance.getUserRequest();
                userRequest.addFailReason("instance" + instance.getId());
                failedUserRequests.add(userRequest);
                instanceIterator.remove();
            }
        }

        innerScheduler.addInstance(instances, true);

        if (isNeedRevertSelfHostState) {
            statesManager.revertSelftHostState(instances, innerScheduler);
        }

        LOGGER.warn("{}: {}'s {} failed to schedule {} instances,it need retry soon.", getSimulation().clockStr(), getName(), innerScheduler.getName(), instances.size());

        if (failedUserRequests.size() > 0) {
            send(getSimulation().getCis(), 0, CloudSimTag.USER_REQUEST_FAIL, failedUserRequests);
        }
    }

    /**
     * Retrieve instances from the {@link InstanceQueue} and publish them to various {@link InnerScheduler}s.
     *
     * @param evt
     */
    private void processLoadBalanceSend(SimEvent evt) {
        List<Instance> instances = instanceQueue.getAllItem(true);
        if (instances.size() != 0) {
            Set<InnerScheduler> sentInnerScheduler = loadBalance.sendInstances(instances);
            if (instanceQueue.size() > 0) {
                send(this, loadBalance.getLoadBalanceCostTime(), CloudSimTag.LOAD_BALANCE_SEND, null);
            }
            for (InnerScheduler innerScheduler : sentInnerScheduler) {
                if (!isInnerSchedulerBusy.containsKey(innerScheduler) || !isInnerSchedulerBusy.get(innerScheduler)) {
                    send(this, loadBalance.getLoadBalanceCostTime(), CloudSimTag.INNER_SCHEDULE_BEGIN, innerScheduler);
                    isInnerSchedulerBusy.put(innerScheduler, true);
                }
            }
        }
    }

    /**
     * Tell the datacenter not to place InstanceGroups in that datacenter
     *
     * @param evt the data of the event is a List of InstanceGroups.
     */
    private void processRespondDcReviveGroupGiveUp(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroupsTmp) {
            List<InstanceGroup> instanceGroups = (List<InstanceGroup>) instanceGroupsTmp;
            instanceGroups.removeIf(instanceGroup -> instanceGroup.getUserRequest().getState() == UserRequest.FAILED);
            interScheduler.receiveNotEmployGroup(instanceGroups);
        }
    }

    private void processScheduleToDcHostResponse(SimEvent evt) {
        Datacenter sourceDc = (Datacenter) evt.getSource();
        interScheduler.receiveReplyFromDatacenter(sourceDc);

        if (evt.getTag() == CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED) {
            List<InstanceGroup> failedInstanceGroups = (List<InstanceGroup>) evt.getData();
            interScheduler.addInstanceGroups(failedInstanceGroups, true);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}: {}'s {} failed to schedule {} instanceGroups,it need retry soon.", getSimulation().clockStr(), getName(), interScheduler.getName(), failedInstanceGroups.size());
            }
        }

        if (interScheduler.isAllReplyReceived()) {
            LOGGER.info("{}: {}'s all reply from datacenter have been received.", getSimulation().clockStr(), getName());

            startInterScheduling();
        }
    }

    private void processScheduleToDcHost(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroupsTmp) {
            List<InstanceGroup> instanceGroups = (List<InstanceGroup>) instanceGroupsTmp;
            instanceGroups.removeIf(instanceGroup -> instanceGroup.getUserRequest().getState() == UserRequest.FAILED);

            List<InstanceGroup> failedInstanceGroups = allocateBwForInstanceGroups(instanceGroups);
            instanceGroups.removeAll(failedInstanceGroups);

            List<InstanceGroup> conflictedInstanceGroup = resourceAllocateSelector.filterConflictedInstanceGroup(instanceGroups);
            instanceGroups.removeAll(conflictedInstanceGroup);
            failedInstanceGroups.addAll(conflictedInstanceGroup);
            revertBwForInstanceGroups(conflictedInstanceGroup);

            List<InstanceGroup> allocateFailedInstanceGroups = allocateResourceForInstanceGroups(instanceGroups);
            instanceGroups.removeAll(allocateFailedInstanceGroups);
            failedInstanceGroups.addAll(allocateFailedInstanceGroups);
            revertBwForInstanceGroups(allocateFailedInstanceGroups);

            SimEntity source = evt.getSource();
            if (failedInstanceGroups.size() > 0) {
                send(source, 0, CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED, failedInstanceGroups);
            } else {
                send(source, 0, CloudSimTag.SCHEDULE_TO_DC_HOST_OK, null);
            }

            markAndRecordInstanceGroups(instanceGroups);
            getSimulation().getSqlRecord().recordInstanceGroupsGraph(instanceGroups);
            getSimulation().getSqlRecord().recordInstancesCreateInfo(instanceGroups);
        }
    }

    private void revertBwForInstanceGroups(List<InstanceGroup> instanceGroups) {
        for (InstanceGroup instanceGroup : instanceGroups) {
            revertBwForInstanceGroup(instanceGroup);
            instanceGroup.setReceiveDatacenter(Datacenter.NULL);
        }
    }

    private void revertBwForInstanceGroup(InstanceGroup instanceGroup) {
        UserRequest userRequest = instanceGroup.getUserRequest();
        List<InstanceGroup> dstInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dst : dstInstanceGroups) {
            if (dst.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(instanceGroup, dst);
                getSimulation().getNetworkTopology().releaseBw(instanceGroup.getReceiveDatacenter(), dst.getReceiveDatacenter(), edge.getRequiredBw());
                userRequest.delAllocatedEdge(edge);
            }
        }

        List<InstanceGroup> srcInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup src : srcInstanceGroups) {
            if (src.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(src, instanceGroup);
                getSimulation().getNetworkTopology().releaseBw(src.getReceiveDatacenter(), instanceGroup.getReceiveDatacenter(), edge.getRequiredBw());
                userRequest.delAllocatedEdge(edge);
            }
        }
    }

    private void markAndRecordInstanceGroups(List<InstanceGroup> instanceGroups) {
        instanceGroups.forEach(instanceGroup -> instanceGroup.setReceivedTime(getSimulation().clock()));
        getSimulation().getSqlRecord().recordInstanceGroupsReceivedInfo(instanceGroups);
    }

    private List<InstanceGroup> allocateBwForInstanceGroups(List<InstanceGroup> instanceGroups) {
        List<InstanceGroup> failedInstanceGroups = new ArrayList<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            if (!allocateBwForGroup(instanceGroup, this)) {
                failedInstanceGroups.add(instanceGroup);
            } else {
                instanceGroup.setReceiveDatacenter(this);
            }
        }
        return failedInstanceGroups;
    }

    private List<InstanceGroup> allocateResourceForInstanceGroups(List<InstanceGroup> instanceGroups) {
        List<InstanceGroup> failedInstanceGroups = new ArrayList<>();
        Map<Integer, List<Instance>> lifeInstancesMap = new HashMap<>();

        for (InstanceGroup instanceGroup : instanceGroups) {
            for (Instance instance : instanceGroup.getInstances()) {
                if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                    continue;
                }

                boolean allocateResult = statesManager.allocate(instance);
                if (!allocateResult) {
                    LOGGER.warn("{} : {}'s Instance{} failed to allocate resources on host{} after resourceAllocateSelector", getSimulation().clockStr(), getName(), instance.getId(), instance.getExpectedScheduleHostId());
                    failedInstanceGroups.add(instanceGroup);
                }

                updateAfterInstanceAllocated(instance);

                int lifeTime = instance.getLifeTime();
                lifeInstancesMap.putIfAbsent(lifeTime, new ArrayList<>());
                lifeInstancesMap.get(lifeTime).add(instance);
            }
        }

        sendFinishInstanceRunEvt(lifeInstancesMap);

        return failedInstanceGroups;
    }

    private void updateAfterInstanceAllocated(Instance instance) {
        instance.setState(UserRequest.RUNNING);
        instance.setStartTime(getSimulation().clock());
        instance.setHost(instance.getExpectedScheduleHostId());
        instance.setExpectedScheduleHostId(-1);
    }

    private void processScheduleToDcNoForward(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroupsTmp) {
            List<InstanceGroup> instanceGroups = (List<InstanceGroup>) instanceGroupsTmp;
            deleteFailedInstanceGroupAndSetReceivedTime(instanceGroups);

            instanceQueue.add(instanceGroups);

            LOGGER.info("{}: {} receives {}'s respond to employ {} InstanceGroups.Now the size of instanceQueue is {}.",
                    getSimulation().clockStr(),
                    getName(),
                    evt.getSource().getName(),
                    instanceGroups.size(),
                    instanceQueue.size());

            getSimulation().getSqlRecord().recordInstanceGroupsReceivedInfo(instanceGroups);

            sendNow(this, CloudSimTag.LOAD_BALANCE_SEND);
        }
    }

    private void deleteFailedInstanceGroupAndSetReceivedTime(List<InstanceGroup> instanceGroups) {
        Iterator<InstanceGroup> iterator = instanceGroups.iterator();
        while (iterator.hasNext()) {
            InstanceGroup instanceGroup = iterator.next();
            if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
                iterator.remove(); // 使用迭代器的remove()方法安全地删除元素
            } else {
                instanceGroup.setReceivedTime(getSimulation().clock());
            }
        }
    }

    /**
     * Tell the datacenter to place InstanceGroups in that datacenter
     *
     * @param evt the data of the event is a List of InstanceGroups or a List of Instances.
     */
    private void processRespondDcReviveGroupEmploy(SimEvent evt) {
        if (evt.getData() instanceof List<?> instancesTmp) {
            if (instancesTmp.size() == 0) {
                return;
            }
            if (instancesTmp.get(0) instanceof InstanceGroup) {
                List<InstanceGroup> instanceGroups = (List<InstanceGroup>) instancesTmp;

                Iterator<InstanceGroup> iterator = instanceGroups.iterator();
                while (iterator.hasNext()) {
                    InstanceGroup instanceGroup = iterator.next();
                    if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
                        iterator.remove(); // 使用迭代器的remove()方法安全地删除元素
                    } else if (instanceGroup.getReceivedTime() == -1) {
                        instanceGroup.setReceivedTime(getSimulation().clock());
                    }
                }
                if (!isCentralizedInterSchedule()) {
                    interScheduler.receiveEmployGroup(instanceGroups);
                }
                instanceQueue.add(instanceGroups);
                LOGGER.info("{}: {} receives {}'s respond to employ {} InstanceGroups.Now the size of instanceQueue is {}.",
                        getSimulation().clockStr(),
                        getName(),
                        evt.getSource().getName(),
                        instanceGroups.size(),
                        instanceQueue.size());
                getSimulation().getSqlRecord().recordInstanceGroupsReceivedInfo(instanceGroups);
            } else if (instancesTmp.get(0) instanceof Instance) {
                List<Instance> instances = (List<Instance>) instancesTmp;
                instances.removeIf(instance -> instance.getUserRequest().getState() == UserRequest.FAILED);
                instanceQueue.add(instances);
                LOGGER.info("{}: {} receives {}'s respond to employ {} Instances.Now the size of instanceQueue is {}.",
                        getSimulation().clockStr(),
                        getName(),
                        evt.getSource().getName(),
                        instances.size(),
                        instanceQueue.size());
            }
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

    /**
     * the datacenter reply whether they can receive this instanceGroups.
     *
     * @param evt the data of this evt is a list of instanceGroups.
     */
    private void processRespondDcReviveGroup(SimEvent evt) {
        if (evt.getData() instanceof Map<?,?> responseResultTmp) {
            Datacenter srcDatacenter = (Datacenter) evt.getSource();
            Map<InstanceGroup,Double> responseResult = (Map<InstanceGroup,Double>) responseResultTmp;
            List<InstanceGroup> waitDecideInstanceGroups = null;
            for (InstanceGroup instanceGroup : responseResult.keySet()) {
                if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
                    continue;
                }
                instanceGroupSendResultMap.get(instanceGroup).put(srcDatacenter, responseResult.get(instanceGroup));
                if (isAllSendResultReceived(instanceGroup)) {
                    if (waitDecideInstanceGroups == null) {
                        waitDecideInstanceGroups = new ArrayList<>();
                    }
                    waitDecideInstanceGroups.add(instanceGroup);
                }
            }
            if (waitDecideInstanceGroups != null) {
                Map<InstanceGroup, Datacenter> decideResult = interScheduler.decideTargetDatacenter(instanceGroupSendResultMap, waitDecideInstanceGroups);
                double delay = interScheduler.getDecideTargetDatacenterCostTime();
                sendDecideResult(decideResult, delay);
                LOGGER.info("{}: {} decides to schedule {} InstanceGroup after receiving all responds.", getSimulation().clockStr(), getName(), decideResult.size());
            }
        }
    }

    private void sendDecideResult(Map<InstanceGroup, Datacenter> decideResult, double delay) {
        Map<Datacenter, Map<Integer, List<InstanceGroup>>> sendResult = new HashMap<>();
        List<InstanceGroup> failInstanceGroups = new ArrayList<>();
        for (Map.Entry<InstanceGroup, Datacenter> entry : decideResult.entrySet()) {
            InstanceGroup instanceGroup = entry.getKey();
            Datacenter receiveDatacenter = entry.getValue();
            if (receiveDatacenter == null) {
                failInstanceGroups.add(instanceGroup);
            } else {
                instanceGroup.setReceiveDatacenter(receiveDatacenter);
                if (!allocateBwForGroup(instanceGroup, receiveDatacenter)) {
                    failInstanceGroups.add(instanceGroup);
                    continue;
                }
                instanceGroup.setState(UserRequest.SCHEDULING);
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
        //调度失败的InstanceGroup
        if (failInstanceGroups.size() > 0) {
            interScheduleFail(failInstanceGroups);
        }
        for (Map.Entry<Datacenter, Map<Integer, List<InstanceGroup>>> entry : sendResult.entrySet()) {
            Datacenter datacenter = entry.getKey();
            Map<Integer, List<InstanceGroup>> result = entry.getValue();
            for (Map.Entry<Integer, List<InstanceGroup>> entry1 : result.entrySet()) {
                int isEmploy = entry1.getKey();
                List<InstanceGroup> instanceGroups = entry1.getValue();
                if (isEmploy == 1) {
//                    send(datacenter, delay, CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY, instanceGroups);
                } else {
                    send(datacenter, delay, CloudSimTag.RESPOND_DC_REVIVE_GROUP_GIVE_UP, instanceGroups);
                }
            }
        }
    }

    private boolean allocateBwForGroup(InstanceGroup instanceGroup, Datacenter receiveDatacenter) {
        UserRequest userRequest = instanceGroup.getUserRequest();
        List<InstanceGroup> dstInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dst : dstInstanceGroups) {
            if (dst.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(instanceGroup, dst);
                if (!getSimulation().getNetworkTopology().allocateBw(receiveDatacenter, dst.getReceiveDatacenter(), edge.getRequiredBw())) {
                    return false;
                }
                userRequest.addAllocatedEdge(edge);
            }
        }
        List<InstanceGroup> srcInstanceGroups = instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup src : srcInstanceGroups) {
            if (src.getReceiveDatacenter() != Datacenter.NULL) {
                InstanceGroupEdge edge = instanceGroup.getUserRequest().getInstanceGroupGraph().getEdge(src, instanceGroup);
                if (!getSimulation().getNetworkTopology().allocateBw(src.getReceiveDatacenter(), receiveDatacenter, edge.getRequiredBw())) {
                    return false;
                }
                userRequest.addAllocatedEdge(edge);
            }
        }
        return true;
    }


    //TODO 可以优化一下速度
    private boolean isAllSendResultReceived(InstanceGroup instanceGroup) {
        for (Map.Entry<Datacenter, Double> entry : instanceGroupSendResultMap.get(instanceGroup).entrySet()) {
            if (entry.getValue() == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ask if the datacenter can receive the instanceGroups.
     *
     * @param evt The data of evt is a List of InstanceGroups.
     */
    private void processAskDcReviveGroup(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroups) {
            Map<InstanceGroup, Double> reviveGroupResult = interScheduler.decideReciveGroupResult((List<InstanceGroup>) instanceGroups);
            double costTime = interScheduler.getDecideReceiveGroupResultCostTime();
            send(evt.getSource(), costTime, CloudSimTag.RESPOND_DC_REVIVE_GROUP, reviveGroupResult);
            LOGGER.info("{}: {} received {} instanceGroups to mark scores for them.", getSimulation().clockStr(), getName(), instanceGroups.size());
        }
    }

    /**
     * Process the user requests send for this datacenter.
     *
     * @param evt The data can be a List of UserRequest or a List of InstanceGroup, a UserRequest, a InstanceGroup.
     */
    private void processUserRequestsSend(final SimEvent evt) {
        if (evt.getData() instanceof List<?> userRequestsTmp) {
            if (userRequestsTmp.size() == 0) {
                return;
            } else if (userRequestsTmp.get(0) instanceof UserRequest) {
                List<UserRequest> userRequests = (List<UserRequest>) userRequestsTmp;
                interScheduler.addUserRequests(userRequests);

                LOGGER.info("{}: {} received {} user request from {}.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), userRequests.size(), evt.getSource().getName(), interScheduler.getNewQueueSize());
            } else if (userRequestsTmp.get(0) instanceof InstanceGroup) {
                List<InstanceGroup> instanceGroups = (List<InstanceGroup>) userRequestsTmp;
                interScheduler.addInstanceGroups(instanceGroups, false);

                LOGGER.info("{}: {} received {} instanceGroups from {}.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), instanceGroups.size(), evt.getSource().getName(), interScheduler.getNewQueueSize());
            }
        }
        if (!isGroupFilterDcBusy) {
            sendNow(this, CloudSimTag.GROUP_FILTER_DC_BEGIN);
            isGroupFilterDcBusy = true;//放在这里可以防止同一时间多次触发
        }
    }

    /**
     * Assign a datacenter to InstanceGroup, but at this point, the results were not sent out.
     * It will send an GROUP_FILTER_DC_END evt to call {!link #processGroupAssignDcEnd(SimEvent)} after the {@link InterScheduler#getScheduleTime}
     */
    private void processGroupFilterDcBegin() {
        InterSchedulerResult interSchedulerResult = interScheduler.schedule();
        double filterSuitableDatacenterCostTime = interScheduler.getScheduleTime();
        send(this, filterSuitableDatacenterCostTime, CloudSimTag.GROUP_FILTER_DC_END, interSchedulerResult);
        LOGGER.info("{}: {} starts inter scheduling.It costs {}ms.", getSimulation().clockStr(), getName(), filterSuitableDatacenterCostTime);
    }

    private void processAskSimpleState(final SimEvent evt) {
        Object simpleState = statesManager.getSimpleState().generate(this);
        send(evt.getSource(), 0, CloudSimTag.RESPOND_SIMPLE_STATE, simpleState);
        LOGGER.info("{}: {} sends the simple state of itself to {}.", getSimulation().clockStr(), getName(), evt.getSource().getName());
    }

    private void processRespondSimpleState(final SimEvent evt) {
        if (evt.getData() != null) {
            interScheduler.getInterScheduleSimpleStateMap().put((Datacenter) evt.getSource(), evt.getData());
            if (interScheduler.getInterScheduleSimpleStateMap().containsValue(null)) {
                return;
            }
            startInterSchedule();
        }
    }

    private void startInterSchedule() {
        interScheduler.getInterScheduleSimpleStateMap().put(this, statesManager.getSimpleState().generate(this));

        List<InstanceGroup> instanceGroups = groupQueue.getBatchItem();
        if (instanceGroups.size() == 0) {
            return;
        }
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = interScheduler.filterSuitableDatacenter(instanceGroups);
        double filterSuitableDatacenterCostTime = interScheduler.getScheduleTime();
        send(this, filterSuitableDatacenterCostTime, CloudSimTag.GROUP_FILTER_DC_END, instanceGroupAvailableDatacenters);
        LOGGER.info("{}: {} starts finding available Datacenters for {} instance groups.It costs {}ms.", getSimulation().clockStr(), getName(), instanceGroups.size(), filterSuitableDatacenterCostTime);
    }

    /**
     * Send instanceGroups to various datacenters based on the results.
     *
     * @param evt The data can be a Map of InstanceGroup and List of Datacenter.It is the result of {!link #processGroupFilterDcBegin()}.
     */
    private void processGroupFilterDcEnd(final SimEvent evt) {
        if (evt.getData() instanceof InterSchedulerResult interSchedulerResult) {
            sendInterScheduleResult(interSchedulerResult);

            LOGGER.info("{}: {} ends finding available Datacenters for {} instance groups.", getSimulation().clockStr(), getName(), interSchedulerResult.getScheduledResultMap().size());

            if (isScheduleToSelfEmpty(interSchedulerResult)) {
                startInterScheduling();
            }
        }
    }

    private boolean isScheduleToSelfEmpty(InterSchedulerResult interSchedulerResult) {
        Map<Datacenter, List<InstanceGroup>> scheduledResultMap = interSchedulerResult.getScheduledResultMap();
        return !scheduledResultMap.containsKey(this) || scheduledResultMap.get(this).size() == 0;
    }

    private void startInterScheduling() {
        if (interScheduler.isQueuesEmpty()) {
            isGroupFilterDcBusy = false;
        } else {
            send(this, 0, CloudSimTag.GROUP_FILTER_DC_BEGIN, null);
        }
    }

    private void sendInterScheduleResult(InterSchedulerResult interSchedulerResult) {
        if (interSchedulerResult.getTarget() == InterSchedulerSimple.MIXED_TARGET) {
            interScheduler.clearReplyWaitingDatacenter();
            Map<Datacenter, List<InstanceGroup>> scheduledResultMap = interSchedulerResult.getScheduledResultMap();
            for (Map.Entry<Datacenter, List<InstanceGroup>> entry : scheduledResultMap.entrySet()) {
                Datacenter datacenter = entry.getKey();
                List<InstanceGroup> instanceGroups = entry.getValue();

                if (datacenter == this) {
                    if (instanceGroups.size() > 0) {
                        send(this, 0, CloudSimTag.SCHEDULE_TO_DC_HOST, instanceGroups);
                        interScheduler.addReplyWaitingDatacenter(this);
                        LOGGER.info("{}: {} sends {} instance groups to itself.", getSimulation().clockStr(), getName(), instanceGroups.size());
                    }
                } else {
                    if (instanceGroups.size() > 0) {
                        send(datacenter, 0, CloudSimTag.USER_REQUEST_SEND, instanceGroups);
                        addSelfInForwardHistory(instanceGroups);
                        LOGGER.info("{}: {} sends {} instance groups to other datacenter {}.", getSimulation().clockStr(), getName(), instanceGroups.size(), datacenter.getName());
                    }
                }
            }
        }
    }

    private void addSelfInForwardHistory(List<InstanceGroup> instanceGroups) {
        instanceGroups.forEach(instanceGroup -> instanceGroup.addForwardDatacenterIdHistory(getId()));
    }

    //根据筛选情况进行调度
    private void interScheduleByResult(Map<InstanceGroup, List<Datacenter>> instanceGroupAvaiableDatacenters) {
        boolean isDirectedSend = interScheduler.isDirectedSend();
        Map<Datacenter, List<InstanceGroup>> sendMap = new HashMap<>();//记录每个数据中心需要发送的亲和组以统一发送
        List<InstanceGroup> retryInstanceGroups = new ArrayList<>();
        for (Map.Entry<InstanceGroup, List<Datacenter>> entry : instanceGroupAvaiableDatacenters.entrySet()) {
            InstanceGroup instanceGroup = entry.getKey();
            List<Datacenter> datacenters = entry.getValue();
            if (datacenters.size() == 0) {
                //如果没有可调度的数据中心，那么就要么再次尝试要么设置为失败
                retryInstanceGroups.add(instanceGroup);
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
                    if (!isDirectedSend) {
                        if (instanceGroupSendResultMap.containsKey(instanceGroup)) {
                            instanceGroupSendResultMap.get(instanceGroup).put(datacenter, -1.0);
                        }
                        else {
                            Map<Datacenter, Double> datacenterIntegerMap = new HashMap<>();
                            datacenterIntegerMap.put(datacenter, -1.0);
                            instanceGroupSendResultMap.put(instanceGroup, datacenterIntegerMap);
                        }
                    }
                }
            }
        }
        //向每个dc以list的形式发送instanceGroups
        for (Map.Entry<Datacenter, List<InstanceGroup>> entry : sendMap.entrySet()) {
            Datacenter datacenter = entry.getKey();
            List<InstanceGroup> instanceGroups = entry.getValue();
            if (isDirectedSend) {
                for(InstanceGroup instanceGroup:instanceGroups){
                    instanceGroup.setReceiveDatacenter(datacenter);
                    if (!allocateBwForGroup(instanceGroup, datacenter)) {
                        retryInstanceGroups.add(instanceGroup);
                        continue;
                    }
                    instanceGroup.setState(UserRequest.SCHEDULING);
                }
//                send(datacenter, 0, CloudSimTag.RESPOND_DC_REVIVE_GROUP_EMPLOY, instanceGroups);
            } else {
                send(datacenter, 0, CloudSimTag.ASK_DC_REVIVE_GROUP, instanceGroups);
            }
        }
        //处理调度失败的instanceGroup
        interScheduleFail(retryInstanceGroups);
    }

    private void interScheduleFail(List<InstanceGroup> instanceGroups) {
        List<InstanceGroup> retryInstanceGroups = new ArrayList<>();
        Set<UserRequest> failedUserRequests = new HashSet<>();

        for (InstanceGroup instanceGroup : instanceGroups) {
            //如果重试次数增加了之后没有超过最大重试次数，那么就将其重新放入队列中等待下次调度
            instanceGroup.addRetryNum();
            if (instanceGroup.isFailed()) {
                instanceGroup.getUserRequest().addFailReason("InstanceGroup" + instanceGroup.getId());
                failedUserRequests.add(instanceGroup.getUserRequest());
            } else {
                retryInstanceGroups.add(instanceGroup);
            }
        }

        if (retryInstanceGroups.size() > 0) {
            send(this, 0, CloudSimTag.USER_REQUEST_SEND, retryInstanceGroups);
            LOGGER.warn("{}: {}'s {} instance groups retry.", getSimulation().clockStr(), getName(), retryInstanceGroups.size());
        }

        if (failedUserRequests.size() > 0) {
            send(getSimulation().getCis(), 0, CloudSimTag.USER_REQUEST_FAIL, failedUserRequests);
        }
    }

    @Override
    public boolean isCentralizedInterSchedule() {
        return centralizedInterSchedule;
    }

    @Override
    public double getEstimatedTCO(InstanceGroup instanceGroup) {
        double tco = 0;
        for (Instance instance : instanceGroup.getInstances()) {
            tco += instance.getCpu() * unitCpuPrice * instance.getLifeTime() / 1000.0
                    + instance.getRam() * unitRamPrice * instance.getLifeTime() / 1000.0
                    + instance.getStorage() * unitStoragePrice * instance.getLifeTime() / 1000.0
                    + instance.getBw() * unitBwPrice * instance.getLifeTime() / 1000.0
                    + (double) instance.getCpu() / statesManager.getMaxCpuCapacity() * unitRackPrice;
        }
        return tco;
    }

    @Override
    public int compareTo(SimEntity o) {
        return Comparator.comparing(SimEntity::getId).compare(this, o);
    }

    private void updateTCO() {
        TCOEnergy = statesManager.getTotalCpuInUse() * unitCpuPrice
                + statesManager.getTotalStorageInUse() * unitStoragePrice;
        TCORack = Math.ceil((double) statesManager.getTotalCpuInUse() / cpuNumPerRack) * unitRackPrice;
    }
}
