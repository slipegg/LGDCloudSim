package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.cpnsim.core.CloudSimEntity;
import org.cpnsim.core.CloudSimTag;
import org.cpnsim.core.SimEntity;
import org.cpnsim.core.Simulation;
import org.cpnsim.core.events.SimEvent;
import org.cpnsim.intrascheduler.IntraSchedulerResult;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.interscheduler.InterSchedulerResult;
import org.cpnsim.interscheduler.InterSchedulerSimple;
import org.cpnsim.intrascheduler.IntraScheduler;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupEdge;
import org.cpnsim.request.UserRequest;
import org.cpnsim.statemanager.StatesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
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

    @Getter
    @Setter
    private String region;

    @Getter
    private Point2D location;

    @Getter
    @Setter
    private String architecture = "";

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
     * See {@link IntraScheduler}.
     */
    @Getter
    private List<IntraScheduler> intraSchedulers;

    /**
     * See {@link LoadBalance}.
     */
    @Getter
    private LoadBalance loadBalance;

    /**
     * See {@link ConflictHandler}.
     */
    @Getter
    private ConflictHandler conflictHandler;

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
     * The IntraScheduleResult List.
     **/
    private List<IntraSchedulerResult> intraSchedulerResults;

    /**
     * The instanceGroup SendResult Map.It is used for inter scheduler.
     **/
    private Map<InstanceGroup, Map<Datacenter, Double>> instanceGroupSendResultMap;

    /**
     * Whether the interScheduler is busy.
     **/
    private boolean isGroupFilterDcBusy = false;

    /**
     * Whether the intraScheduler is busy.
     **/
    private Map<IntraScheduler, Boolean> isIntraSchedulerBusy = new HashMap<>();

    public DatacenterSimple(@NonNull Simulation simulation) {
        super(simulation);
        this.collaborationIds = new HashSet<>();
        this.instanceQueue = new InstanceQueueFifo();
        this.instanceGroupSendResultMap = new HashMap<>();
        this.intraSchedulerResults = new ArrayList<>();
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

    public Datacenter setIntraSchedulers(List<IntraScheduler> intraSchedulers) {
        this.intraSchedulers = intraSchedulers;
        for (IntraScheduler intraScheduler : intraSchedulers) {
            intraScheduler.setDatacenter(this);
        }
        return this;
    }

    @Override
    public Datacenter setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
        loadBalance.setDatacenter(this);
        return this;
    }

    public Datacenter setConflictHandler(ConflictHandler conflictHandler) {
        this.conflictHandler = conflictHandler;
        conflictHandler.setDatacenter(this);
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

        if (intraSchedulers != null && !intraSchedulers.isEmpty()) {
            statesManager.adjustScheduleView();
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
            case CloudSimTag.USER_REQUEST_SEND -> processUserRequestsSend(evt);
            case CloudSimTag.INTER_SCHEDULE_BEGIN -> processInterScheduleBegin();
            case CloudSimTag.INTER_SCHEDULE_END -> processInterScheduleEnd(evt);
            case CloudSimTag.SCHEDULE_TO_DC_NO_FORWARD -> processScheduleToDcNoForward(evt);
            case CloudSimTag.SCHEDULE_TO_DC_HOST -> processScheduleToDcHost(evt);
            case CloudSimTag.SCHEDULE_TO_DC_HOST_OK, CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED ->
                    processScheduleToDcHostResponse(evt);
            case CloudSimTag.LOAD_BALANCE_SEND -> processLoadBalanceSend(evt);//负载均衡花费时间，不形成瓶颈
            case CloudSimTag.INTRA_SCHEDULE_BEGIN -> processInnerScheduleBegin(evt);
            case CloudSimTag.INTRA_SCHEDULE_END -> processInnerScheduleEnd(evt);
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

        if (Objects.equals(this.architecture, "two-level")) {
            statesManager.adjustScheduleView();
            LOGGER.info("{}: {} adjust schedule view, now the view of {} is {}", getSimulation().clockStr(), getName(), intraSchedulers.get(0), statesManager.getIntraSchedulerView(intraSchedulers.get(0)));
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn("{}: {}'s Instance{} is terminated prematurely on host{} and resources have been released", getSimulation().clockStr(), getName(), instance.getId(), hostId);
            }
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
     * Place instances on the host based on the results of the {@link IntraScheduler} and the strategy of the {@link ConflictHandler}.
     *
     * @param evt
     */
    private void processPreAllocateResource(SimEvent evt) {
        LOGGER.info("{}: {}'s all intraScheduler results have been collected.it is dealing with scheduling conflicts...", getSimulation().clockStr(), getName());
        ConflictHandlerResult allocateResult = conflictHandler.filterConflictedInstance(this.intraSchedulerResults);

        Map<IntraScheduler, List<Instance>> failedAllocatedRes = allocateResource(allocateResult.getSuccessRes());

        allocateResult.addAllocateFailRes(failedAllocatedRes);

        for (IntraScheduler intraScheduler : allocateResult.getFailedOutdatedResultMap().keySet()) {
            List<Instance> failedInstances = allocateResult.getFailedOutdatedResultMap().get(intraScheduler).getFailRes();
            Set<UserRequest> outDatedUserRequests = allocateResult.getFailedOutdatedResultMap().get(intraScheduler).getOutdatedRequests();
            intraScheduleFailed(failedInstances, intraScheduler, true, outDatedUserRequests);
        }

        for (IntraSchedulerResult intraSchedulerResult : this.intraSchedulerResults) {
            IntraScheduler intraScheduler = intraSchedulerResult.getIntraScheduler();
            startInnerScheduling(intraScheduler);
        }

        this.intraSchedulerResults.clear();
    }

    private Map<IntraScheduler, List<Instance>> allocateResource(Map<IntraScheduler, List<Instance>> allocatedResult) {
        LOGGER.info("{}: {} is allocate resource.", getSimulation().clockStr(), getName());
        Map<Integer, List<Instance>> successAllocatedInstances = new HashMap<>();
        Map<IntraScheduler, List<Instance>> failedInstances = new HashMap<>();

        for (Map.Entry<IntraScheduler, List<Instance>> entry : allocatedResult.entrySet()) {
            IntraScheduler intraScheduler = entry.getKey();
            List<Instance> instances = entry.getValue();
            for (Instance instance : instances) {
                if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                    continue;
                }

                int allocatedHostId = instance.getExpectedScheduleHostId();
                if (!statesManager.allocate(allocatedHostId, instance))//理论上到这里不会出现分配失败的情况
                {
                    LOGGER.warn("{}: {}'s Instance{} failed to allocate resources on host{} after processPreAllocateResource", getSimulation().clockStr(), getName(), instance.getId(), instance.getExpectedScheduleHostId());
                    failedInstances.putIfAbsent(intraScheduler, new ArrayList<>());
                    failedInstances.get(intraScheduler).add(instance);
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
     * Publish the results of the {@link IntraScheduler}
     *
     * @param evt the data of the event is {@link IntraSchedulerResult}
     */
    private void processInnerScheduleEnd(SimEvent evt) {
        if (evt.getData() instanceof IntraSchedulerResult intraSchedulerResult) {
            IntraScheduler intraScheduler = intraSchedulerResult.getIntraScheduler();

            LOGGER.info("{}: {}'s {} ends scheduling instances.", getSimulation().clockStr(), getName(), intraScheduler.getName());

            if (!statesManager.isInLatestSmallSynGap(intraSchedulerResult.getScheduleTime())) {//把同步时对这一调度的记录补回来
                statesManager.revertHostState(intraSchedulerResult);
            }

            if (!intraSchedulerResult.isFailedInstancesEmpty()) {
                intraScheduleFailed(intraSchedulerResult.getFailedInstances(), intraScheduler, false, intraSchedulerResult.getOutDatedUserRequests());
            }
            intraSchedulerResults.add(intraSchedulerResult);
            if (!intraSchedulerResult.isScheduledInstancesEmpty()) {
                send(this, 0, CloudSimTag.PRE_ALLOCATE_RESOURCE, null);
            } else {
                isIntraSchedulerBusy.put(intraScheduler, false);
            }
//            if (intraScheduler.getNewInstanceQueueSize() != 0) {
//                send(this, 0, CloudSimTag.INNER_SCHEDULE_BEGIN, intraScheduler);
//            } else {
//                isIntraSchedulerBusy.put(intraScheduler, false);
//            }
        }
    }

    private void startInnerScheduling(IntraScheduler intraScheduler) {
        if (intraScheduler.isQueuesEmpty()) {
            isIntraSchedulerBusy.put(intraScheduler, false);
        } else if (!intraScheduler.isQueuesEmpty()) {
            send(this, 0, CloudSimTag.INTRA_SCHEDULE_BEGIN, intraScheduler);
        }
    }

    /**
     * Call the {@link IntraScheduler} to allocate {@link Instance} to various hosts.
     * It will send INNER_SCHEDULE_END after {@link IntraScheduler#getScheduleCostTime()}
     *
     * @param evt
     */
    private void processInnerScheduleBegin(SimEvent evt) {
        if (evt.getData() instanceof IntraScheduler intraScheduler) {
            IntraSchedulerResult innerScheduleResult = intraScheduler.schedule();

            double costTime = intraScheduler.getScheduleCostTime();

            LOGGER.info("{}: {}'s {} starts scheduling {} instances,cost {} ms", getSimulation().clockStr(), this.getName(), intraScheduler.getName(), innerScheduleResult.getInstanceNum(), costTime);

            send(this, costTime, CloudSimTag.INTRA_SCHEDULE_END, innerScheduleResult);
        }
    }

    private void intraScheduleFailed(List<Instance> instances, IntraScheduler intraScheduler, boolean isNeedRevertSelfHostState) {
        Set<UserRequest> outDatedUserRequests = new HashSet<>();
        intraScheduleFailed(instances, intraScheduler, isNeedRevertSelfHostState, outDatedUserRequests);
    }

    private void intraScheduleFailed(List<Instance> instances, IntraScheduler intraScheduler, boolean isNeedRevertSelfHostState, Set<UserRequest> outDatedUserRequests) {
        Set<UserRequest> failedUserRequests = outDatedUserRequests;
        for (UserRequest userRequest : outDatedUserRequests) {
            userRequest.addFailReason("outDated");
        }

        Iterator<Instance> instanceIterator = instances.iterator();
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

        intraScheduler.addInstance(instances, true);

        if (isNeedRevertSelfHostState) {
            statesManager.revertSelftHostState(instances, intraScheduler);
        }

        if(instances.size()>0){
            LOGGER.warn("{}: {}'s {} failed to schedule {} instances,it need retry soon.", getSimulation().clockStr(), getName(), intraScheduler.getName(), instances.size());
        }
        if (failedUserRequests.size() > 0) {
            send(getSimulation().getCis(), 0, CloudSimTag.USER_REQUEST_FAIL, failedUserRequests);
        }
    }

    /**
     * Retrieve instances from the {@link InstanceQueue} and publish them to various {@link IntraScheduler}s.
     *
     * @param evt
     */
    private void processLoadBalanceSend(SimEvent evt) {
        List<Instance> instances = instanceQueue.getAllItem();
        if (instances.size() != 0) {
            Set<IntraScheduler> sentIntraScheduler = loadBalance.sendInstances(instances);
            if (instanceQueue.size() > 0) {
                send(this, loadBalance.getLoadBalanceCostTime(), CloudSimTag.LOAD_BALANCE_SEND, null);
            }
            for (IntraScheduler intraScheduler : sentIntraScheduler) {
                if (!isIntraSchedulerBusy.containsKey(intraScheduler) || !isIntraSchedulerBusy.get(intraScheduler)) {
                    send(this, loadBalance.getLoadBalanceCostTime(), CloudSimTag.INTRA_SCHEDULE_BEGIN, intraScheduler);
                    isIntraSchedulerBusy.put(intraScheduler, true);
                }
            }
        }
    }

    private void processScheduleToDcHostResponse(SimEvent evt) {
        if (evt.getTag() == CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED) {
            FailedOutdatedResult<InstanceGroup> failedOutdatedResult = (FailedOutdatedResult<InstanceGroup>) evt.getData();
            handleFailedInterScheduling(failedOutdatedResult.getFailRes(), failedOutdatedResult.getOutdatedRequests());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}: {}'s {} failed to schedule {} instanceGroups,it need retry soon.", getSimulation().clockStr(), getName(), interScheduler.getName(), failedOutdatedResult.getFailRes().size());
            }
        }
    }

    // TODO 这部分逻辑需要检查
    private void processScheduleToDcHost(SimEvent evt) {
        if (evt.getData() instanceof List<?> instanceGroupsTmp) {
            List<InstanceGroup> instanceGroups = (List<InstanceGroup>) instanceGroupsTmp;
            instanceGroups.removeIf(instanceGroup -> instanceGroup.getUserRequest().getState() == UserRequest.FAILED);

            List<InstanceGroup> failedInstanceGroups = allocateBwForInstanceGroups(instanceGroups);
            instanceGroups.removeAll(failedInstanceGroups);

            FailedOutdatedResult<InstanceGroup> conflictedRes = conflictHandler.filterConflictedInstanceGroup(instanceGroups);
            Set<UserRequest> outDatedUserRequests = conflictedRes.getOutdatedRequests();
            instanceGroups.removeAll(conflictedRes.getFailRes());
            instanceGroups.removeIf(instanceGroup -> outDatedUserRequests.contains(instanceGroup.getUserRequest()));
            failedInstanceGroups.addAll(conflictedRes.getFailRes());
            revertBwForInstanceGroups(instanceGroups, conflictedRes.getFailRes(), outDatedUserRequests);

            List<InstanceGroup> allocateFailedInstanceGroups = allocateResourceForInstanceGroups(instanceGroups);
            instanceGroups.removeAll(allocateFailedInstanceGroups);
            failedInstanceGroups.addAll(allocateFailedInstanceGroups);
            revertBwForInstanceGroups(allocateFailedInstanceGroups);

            SimEntity source = evt.getSource();
            if (failedInstanceGroups.size() > 0 || conflictedRes.getOutdatedRequests().size() > 0) {
                send(source, 0, CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED, new FailedOutdatedResult<InstanceGroup>(failedInstanceGroups, outDatedUserRequests));
            } else {
                send(source, 0, CloudSimTag.SCHEDULE_TO_DC_HOST_OK, null);
            }

            if (source == this) {
                startInterScheduling();
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

    private void revertBwForInstanceGroups(List<InstanceGroup> instanceGroups, List<InstanceGroup> failedInstanceGroups, Set<UserRequest> outDatedUserRequests) {
        for (InstanceGroup instanceGroup : failedInstanceGroups) {
            revertBwForInstanceGroup(instanceGroup);
            instanceGroup.setReceiveDatacenter(Datacenter.NULL);
        }

        for (InstanceGroup instanceGroup : instanceGroups) {
            if (outDatedUserRequests.contains(instanceGroup.getUserRequest())) {
                revertBwForInstanceGroup(instanceGroup);
                instanceGroup.setReceiveDatacenter(Datacenter.NULL);
            }
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
        instanceGroups.forEach(instanceGroup -> instanceGroup.setState(UserRequest.SCHEDULING));
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
                    LOGGER.error("{}: {}'s Instance{} of UserRequest{} failed to allocate resources on host{} after resourceAllocateSelector", getSimulation().clockStr(), getName(), instance.getId(), instance.getUserRequest().getId(), instance.getExpectedScheduleHostId());
                    failedInstanceGroups.add(instanceGroup);
                    continue;
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

    /**
     * Process the user requests send for this datacenter.
     *
     * @param evt The data can be a List of UserRequest or a List of InstanceGroup, a UserRequest, a InstanceGroup.
     */
    private void processUserRequestsSend(final SimEvent evt) {
        if (evt.getData() instanceof List<?> userRequestsTmp) {
            if (userRequestsTmp.size() == 0) {
                return;
            }

            if(getSimulation().isSingleDatacenterFlag()){
                acceptUserRequestForSingleDatacenter((List<UserRequest>)userRequestsTmp);
                LOGGER.info("{}: {} received {} userRequests from {}.The size of Instance queue is {}.", getSimulation().clockStr(), getName(), userRequestsTmp.size(), evt.getSource().getName(), instanceQueue.size());
            }else{
                acceptUserRequestForMultiDatacenters(userRequestsTmp);
                LOGGER.info("{}: {} received {} userRequests from {}.The size of InstanceGroup queue is {}.", getSimulation().clockStr(), getName(), userRequestsTmp.size(), evt.getSource().getName(), interScheduler.getNewQueueSize());
            }
        }
    }

    private void acceptUserRequestForSingleDatacenter(List<UserRequest> userRequests){
        instanceQueue.add(userRequests);
        userRequests.forEach(userRequest -> {
            userRequest.getInstanceGroups().forEach(instanceGroup -> {
                instanceGroup.setReceiveDatacenter(this).setReceivedTime(getSimulation().clock()).setState(UserRequest.SCHEDULING);
            });
        });

        getSimulation().getSqlRecord().recordInstanceGroupsReceivedInfo(userRequests);

        sendNow(this, CloudSimTag.LOAD_BALANCE_SEND);
    }

    private void acceptUserRequestForMultiDatacenters(List<?> userRequestsTmp){
        if (userRequestsTmp.get(0) instanceof UserRequest) {
            List<UserRequest> userRequests = (List<UserRequest>) userRequestsTmp;

            interScheduler.addUserRequests(userRequests);
        } else if (userRequestsTmp.get(0) instanceof InstanceGroup) {
            List<InstanceGroup> instanceGroups = (List<InstanceGroup>) userRequestsTmp;

            interScheduler.addInstanceGroups(instanceGroups, false);
        }

        if (!isGroupFilterDcBusy) {
            sendNow(this, CloudSimTag.INTER_SCHEDULE_BEGIN);
            isGroupFilterDcBusy = true;//放在这里可以防止同一时间多次触发
        }
    }

    /**
     * Assign a datacenter to InstanceGroup, but at this point, the results were not sent out.
     * It will send an GROUP_FILTER_DC_END evt to call {!link #processGroupAssignDcEnd(SimEvent)} after the {@link InterScheduler#getScheduleTime}
     */
    private void processInterScheduleBegin() {
        InterSchedulerResult interSchedulerResult = interScheduler.schedule();
        double filterSuitableDatacenterCostTime = interScheduler.getScheduleTime();
        send(this, filterSuitableDatacenterCostTime, CloudSimTag.INTER_SCHEDULE_END, interSchedulerResult);
        LOGGER.info("{}: {} starts inter scheduling.It costs {}ms.", getSimulation().clockStr(), getName(), filterSuitableDatacenterCostTime);
    }

    /**
     * Send instanceGroups to various datacenters based on the results.
     *
     * @param evt The data can be a Map of InstanceGroup and List of Datacenter.It is the result of {!link #processGroupFilterDcBegin()}.
     */
    private void processInterScheduleEnd(final SimEvent evt) {
        if (evt.getData() instanceof InterSchedulerResult interSchedulerResult) {
            sendInterScheduleResult(interSchedulerResult);

            handleFailedInterScheduling(interSchedulerResult.getFailedInstanceGroups(), interSchedulerResult.getOutDatedUserRequests());

            LOGGER.info("{}: {} ends finding available Datacenters for {} instance groups.", getSimulation().clockStr(), getName(), interSchedulerResult.getInstanceGroupNum());

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
            send(this, 0, CloudSimTag.INTER_SCHEDULE_BEGIN, null);
        }
    }

    private void sendInterScheduleResult(InterSchedulerResult interSchedulerResult) {
        if (interSchedulerResult.getTarget() == InterSchedulerSimple.MIXED_TARGET) {
            Map<Datacenter, List<InstanceGroup>> scheduledResultMap = interSchedulerResult.getScheduledResultMap();
            for (Map.Entry<Datacenter, List<InstanceGroup>> entry : scheduledResultMap.entrySet()) {
                Datacenter datacenter = entry.getKey();
                List<InstanceGroup> instanceGroups = entry.getValue();

                if (datacenter == this) {
                    if (instanceGroups.size() > 0) {
                        send(this, 0, CloudSimTag.SCHEDULE_TO_DC_HOST, instanceGroups);
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

    private void handleFailedInterScheduling(List<InstanceGroup> failedInstanceGroups, Set<UserRequest> outDatedUserRequests) {
        List<InstanceGroup> retryInstanceGroups = new ArrayList<>();
        Set<UserRequest> failedUserRequests = outDatedUserRequests;

        for (UserRequest userRequest : outDatedUserRequests) {
            userRequest.addFailReason("outDated");
        }

        for (InstanceGroup instanceGroup : failedInstanceGroups) {
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
            interScheduler.addInstanceGroups(retryInstanceGroups, true);
        }

        if (failedUserRequests.size() > 0) {
            send(getSimulation().getCis(), 0, CloudSimTag.USER_REQUEST_FAIL, failedUserRequests);
            LOGGER.warn("{}: {}'s {} user requests failed.", getSimulation().clockStr(), getName(), failedUserRequests.size());
        }
    }

    private void handleFailedInterScheduling(List<InstanceGroup> failedInstanceGroups) {
        Set<UserRequest> outDatedUserRequests = new HashSet<>();
        handleFailedInterScheduling(failedInstanceGroups, outDatedUserRequests);
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
    public Datacenter setLocation(double latitude, double longitude) {
        location = new Point2D.Double(latitude, longitude);
        return this;
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
