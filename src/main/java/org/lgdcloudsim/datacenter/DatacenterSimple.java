package org.lgdcloudsim.datacenter;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.lgdcloudsim.conflicthandler.ConflictHandler;
import org.lgdcloudsim.conflicthandler.ConflictHandlerResult;
import org.lgdcloudsim.core.CloudSimEntity;
import org.lgdcloudsim.core.CloudSimTag;
import org.lgdcloudsim.core.SimEntity;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.core.events.SimEvent;
import org.lgdcloudsim.intrascheduler.IntraSchedulerResult;
import org.lgdcloudsim.interscheduler.InterScheduler;
import org.lgdcloudsim.interscheduler.InterSchedulerResult;
import org.lgdcloudsim.interscheduler.InterSchedulerSimple;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.loadbalancer.LoadBalancer;
import org.lgdcloudsim.queue.InstanceGroupQueue;
import org.lgdcloudsim.queue.InstanceQueue;
import org.lgdcloudsim.queue.InstanceQueueFifo;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceGroupEdge;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.statemanager.StatesManager;
import org.lgdcloudsim.util.FailedOutdatedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * An interface to be implemented by each class that represents a datacenter.
 * See {@link DatacenterSimple} for an example of how to implement this interface.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class DatacenterSimple extends CloudSimEntity implements Datacenter {
    /**
     * the Logger.
     **/
    public Logger LOGGER = LoggerFactory.getLogger(DatacenterSimple.class.getSimpleName());

    /**
     * the region of the datacenter.
     **/
    @Getter
    @Setter
    private String region;

    /**
     * the location of the datacenter.
     * It can be set or not.
     **/
    @Getter
    private Point2D location;

    /**
     * the architecture of the datacenter.
     * It can be set or not.
     **/
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
     * See {@link LoadBalancer}.
     */
    @Getter
    private LoadBalancer loadBalancer;

    /**
     * See {@link ConflictHandler}.
     */
    @Getter
    private ConflictHandler conflictHandler;

    /**
     * The price per CPU per second.
     * It is used to record the price per second of each CPU occupied by an instance with a finite lifecycle.
     * See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    double pricePerCpuPerSec;

    /**
     * The price per CPU.
     * It is used to record the price of each CPU occupied by an instance with an infinite lifecycle.
     * See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double pricePerCpu;

    /**
     * All cpu expenses, See {@link DatacenterPrice}.
     */
    @Getter
    private double cpuCost;

    /**
     * The price per ram per second.
     * It is used to record the price per second of each ram occupied by an instance with a finite lifecycle.
     * See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double pricePerRamPerSec;

    /**
     * The price per ram.
     * It is used to record the price of each ram occupied by an instance with an infinite lifecycle.
     * See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double pricePerRam;

    /**
     * All ram expenses, See {@link DatacenterPrice}.
     */
    @Getter
    private double ramCost;

    /**
     * The price per storage per second.
     * It is used to record the price per second of each storage occupied by an instance with a finite lifecycle.
     * See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double pricePerStoragePerSec;

    /**
     * The price per storage.
     * It is used to record the price of each storage occupied by an instance with an infinite lifecycle.
     * See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double pricePerStorage;

    /**
     * All storage expenses, See {@link DatacenterPrice}.
     */
    @Getter
    private double storageCost;

    /**
     * The price per bw per second.
     * It is used to record the price per second of each bw occupied by an instance with a finite lifecycle.
     * See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double pricePerBwPerSec;

    /**
     * The price per bw.
     * It is used to record the price of each bw occupied by an instance with an infinite lifecycle.
     * See {@link DatacenterPrice}.
     */
    @Getter
    @Setter
    private double pricePerBw;

    /**
     * All bw expenses, See {@link DatacenterPrice}.
     **/
    @Getter
    private double bwCost;

    /**
     * Number of hosts in a rack.
     **/
    @Getter
    @Setter
    private double hostNumPerRack;

    /**
     * Rental price per rack.
     **/
    @Getter
    @Setter
    private double unitRackPrice;

    /**
     * The billing type of bw resources.
     * It can be "fixed" or "used".
     **/
    @Getter
    @Setter
    private String bwBillingType = "fixed";

    /**
     * The utilization of bw resources.
     **/
    @Getter
    @Setter
    private double bwUtilization;

    /**
     * The flag to set whether the inter-architecture is centralized.
     **/
    @Setter
    private boolean centralizedInterScheduleFlag;

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
    private boolean isInterSchedulerBusy = false;

    /**
     * Whether the intraScheduler is busy.
     **/
    private Map<IntraScheduler, Boolean> isIntraSchedulerBusy = new HashMap<>();

    /**
     * Create a new instance of DatacenterSimple with the given simulation.
     *
     * @param simulation the simulation
     */
    public DatacenterSimple(@NonNull Simulation simulation) {
        super(simulation);
        this.collaborationIds = new HashSet<>();
        this.instanceQueue = new InstanceQueueFifo();
        this.instanceGroupSendResultMap = new HashMap<>();
        this.intraSchedulerResults = new ArrayList<>();
        this.pricePerCpuPerSec = 1.0;
        this.pricePerCpu = 1.0;
        this.pricePerRamPerSec = 1.0;
        this.pricePerRam = 1.0;
        this.pricePerStoragePerSec = 1.0;
        this.pricePerStorage = 1.0;
        this.pricePerBwPerSec = 1.0;
        this.pricePerBw = 1.0;
        this.unitRackPrice = 100.0;
        this.hostNumPerRack = 10;
        this.cpuCost = 0.0;
        this.ramCost = 0.0;
        this.storageCost = 0.0;
        this.bwCost = 0.0;
    }

    /**
     * Create a new instance of DatacenterSimple with the given simulation and id.
     * @param simulation the simulation
     * @param id the id of the datacenter
     */
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
    public Datacenter setIntraSchedulers(List<IntraScheduler> intraSchedulers) {
        this.intraSchedulers = intraSchedulers;
        for (IntraScheduler intraScheduler : intraSchedulers) {
            intraScheduler.setDatacenter(this);
        }
        return this;
    }

    public Datacenter setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
        loadBalancer.setDatacenter(this);
        return this;
    }

    @Override
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
        return maxPowerOnHostNum / hostNumPerRack * unitRackPrice;
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
     * When a datacenter is created, it will send a registration request to the CIS and start the synchronization process.
     */
    @Override
    protected void startInternal() {
        LOGGER.info("{}: {} is starting...", getSimulation().clockStr(), getName());
        sendWithoutNetwork(getSimulation().getCis(), 0, CloudSimTag.DC_REGISTRATION_REQUEST, this);
        if (statesManager.isSynCostTime()) {
            send(this, statesManager.getNextPartitionSynDelay(), CloudSimTag.SYN_STATE_IN_DC, null);
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
     * @see SimEntity#processEvent(SimEvent)
     * @param evt the event
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
            case CloudSimTag.INTRA_SCHEDULE_BEGIN -> processIntraScheduleBegin(evt);
            case CloudSimTag.INTRA_SCHEDULE_END -> processIntraScheduleEnd(evt);
            case CloudSimTag.PRE_ALLOCATE_RESOURCE -> processPreAllocateResource(evt);
            case CloudSimTag.END_INSTANCE_RUN -> processEndInstanceRun(evt);
            default ->
                    LOGGER.warn("{}: {} received unknown event {}", getSimulation().clockStr(), getName(), evt.getTag());
        }
    }


    /**
     * Synchronize the status of other data center.
     * And send a {@link CloudSimTag#SYN_STATE_BETWEEN_DC} event to itself after the specified synchronization interval.
     * @param evt the event
     */
    private void processSynStateBetweenDc(SimEvent evt) {
        if (evt.getData() instanceof List<?> synTargets) {
            if (!synTargets.isEmpty() && synTargets.get(0) instanceof Datacenter) {
                List<Datacenter> datacenters = (List<Datacenter>) synTargets;
                interScheduler.synBetweenDcState(datacenters);
                //TODO In the future, we should consider whether the synchronization time will change midway. If it does, we need to check it. If it never changes, don't change it.
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
            send(this, statesManager.getNextPartitionSynDelay(), CloudSimTag.SYN_STATE_IN_DC, null);
        }

        if (Objects.equals(this.architecture, "two-level")) {
            statesManager.adjustScheduleView();
            LOGGER.info("{}: {} adjust schedule view, now the view of {} is {}", getSimulation().clockStr(), getName(), intraSchedulers.get(0), statesManager.getIntraSchedulerView(intraSchedulers.get(0)));
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
        if (getSimulation().clock() - instance.getStartTime() >= instance.getLifecycle() - 0.01 && instance.getLifecycle() != -1) {
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
        calculateCost(instance);
        updateGroupAndUserRequestState(instance);
    }

    /**
     * Calculate the cost of the instance.
     *
     * @param instance the instance need to be calculated.
     */
    private void calculateCost(Instance instance) {
        if (instance.getStartTime() == -1) {
            return;
        }
        if (instance.getLifecycle() < 0) {
            cpuCost += instance.getCpu() * pricePerCpu;
            ramCost += instance.getRam() * pricePerRam;
            storageCost += instance.getStorage() * pricePerStorage;
        } else {
            double lifeTimeSec = (instance.getFinishTime() - instance.getStartTime()) / 1000.0;
            cpuCost += instance.getCpu() * pricePerCpuPerSec * lifeTimeSec;
            ramCost += instance.getRam() * pricePerRamPerSec * lifeTimeSec;
            storageCost += instance.getStorage() * pricePerStoragePerSec * lifeTimeSec;
        }
        bwCost += calculateInstanceBwCost(instance);
    }

    /**
     * Calculate the cost of the bw resources of the instance.
     * Because the cost of the bw resources is related to the billing type of the bw resources.
     *
     * @param instance the instance need to be calculated.
     * @return the cost of the bw resources of the instance.
     */
    private double calculateInstanceBwCost(Instance instance) {
        double lifeTimeSec = (instance.getFinishTime() - instance.getStartTime()) / 1000.0;
        if (bwBillingType.equals("used")) {
            return (instance.getBw() * lifeTimeSec) / 8 / 1024 * bwUtilization * pricePerBwPerSec;
        } else {
            if (instance.getLifecycle() < 0) {
                return instance.getBw() * pricePerBw;
            } else {
                return instance.getBw() * pricePerBwPerSec * lifeTimeSec;
            }
        }
    }

    /**
     * Update the state of the instance group and the user request.
     * If all instances in the instance group have been successfully run, the instance group is successful.
     * If all instance groups in the user request have been successfully run, the user request is successful.
     * @param instance the instance
     */
    private void updateGroupAndUserRequestState(Instance instance) {
        InstanceGroup instanceGroup = instance.getInstanceGroup();
        if (instance.getState() == UserRequest.SUCCESS) {
            instanceGroup.addSuccessInstanceNum();
        }
        if (instanceGroup.getState() != UserRequest.SUCCESS) {
            return;
        }

        updateInstanceGroupAfterSuccess(instanceGroup);

        //If the InstanceGroup runs successfully, the UserRequest status information needs to be updated.
        UserRequest userRequest = instanceGroup.getUserRequest();
        userRequest.addSuccessGroupNum();
        if (userRequest.getState() == UserRequest.SUCCESS) {
            updateUserRequestAfterSuccess(userRequest);
        }
    }

    /**
     * Update the state of the instance group after all instances in the instance group have been successfully run.
     *
     * @param instanceGroup the instance group
     */
    private void updateInstanceGroupAfterSuccess(InstanceGroup instanceGroup) {
        //All instances of instanceGroup have been successfully run.
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}: {}'s InstanceGroup{} successfully completed running.", getSimulation().clockStr(), getName(), instanceGroup.getId());
        }
        instanceGroup.setFinishTime(getSimulation().clock());
        getSimulation().getSqlRecord().recordInstanceGroupFinishInfo(instanceGroup);

        UserRequest userRequest = instanceGroup.getUserRequest();
        //Release bandwidth resources
        List<InstanceGroup> dstInstanceGroups = userRequest.getInstanceGroupGraph().getDstList(instanceGroup);
        for (InstanceGroup dstInstanceGroup : dstInstanceGroups) {
            if (dstInstanceGroup.getState() == UserRequest.SUCCESS) {
                double releaseBw = userRequest.getInstanceGroupGraph().getBw(instanceGroup, dstInstanceGroup);
                getSimulation().getNetworkTopology().releaseBw(instanceGroup.getReceiveDatacenter(), dstInstanceGroup.getReceiveDatacenter(), releaseBw);
                getSimulation().getSqlRecord().recordInstanceGroupGraphReleaseInfo(instanceGroup.getId(), dstInstanceGroup.getId(), getSimulation().clock());
            }
        }
        List<InstanceGroup> srcInstanceGroups = userRequest.getInstanceGroupGraph().getSrcList(instanceGroup);
        for (InstanceGroup srcInstanceGroup : srcInstanceGroups) {
            if (srcInstanceGroup.getState() == UserRequest.SUCCESS) {
                double releaseBw = userRequest.getInstanceGroupGraph().getBw(srcInstanceGroup, instanceGroup);
                getSimulation().getNetworkTopology().releaseBw(srcInstanceGroup.getReceiveDatacenter(), instanceGroup.getReceiveDatacenter(), releaseBw);
                getSimulation().getSqlRecord().recordInstanceGroupGraphReleaseInfo(srcInstanceGroup.getId(), instanceGroup.getId(), getSimulation().clock());
            }
        }
    }

    /**
     * Update the state of the user request after all instance groups in the user request have been successfully run.
     *
     * @param userRequest the user request
     */
    private void updateUserRequestAfterSuccess(UserRequest userRequest) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}: userRequest{} successfully completed running.", getSimulation().clockStr(), getName());
        }
        userRequest.setFinishTime(getSimulation().clock());
        getSimulation().getSqlRecord().recordUserRequestFinishInfo(userRequest);
    }

    /**
     * Use a {@link ConflictHandler} to check for conflicts before allocating resources and allocate the appropriate instance to the host.
     * The reason for using global intraSchedulerResults is to be able to handle the scheduling results of multiple intra-schedulers at the same time
     * @param evt the event
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
            startIntraScheduling(intraScheduler);
        }

        this.intraSchedulerResults.clear();
    }

    /**
     * Allocate resources to the host based on the results of the {@link IntraScheduler}.
     * @param allocatedResult the result of the resource allocation from the {@link IntraScheduler}
     * @return the failed instances
     */
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

                int lifeTime = instance.getLifecycle();
                successAllocatedInstances.putIfAbsent(lifeTime, new ArrayList<>());
                successAllocatedInstances.get(lifeTime).add(instance);

                if (lifeTime < 0) {
                    calculateCost(instance);
                }
            }
        }

        getSimulation().getSqlRecord().recordInstancesCreateInfo(successAllocatedInstances);
        sendFinishInstanceRunEvt(successAllocatedInstances);

        return failedInstances;
    }

    /**
     * Send the {@link CloudSimTag#END_INSTANCE_RUN} event to itself after the instance has been allocated.
     * Note that the same lifeTime instances are sent in the same event.
     * @param finishInstances
     */
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
    private void processIntraScheduleEnd(SimEvent evt) {
        if (evt.getData() instanceof IntraSchedulerResult intraSchedulerResult) {
            IntraScheduler intraScheduler = intraSchedulerResult.getIntraScheduler();

            LOGGER.info("{}: {}'s {} ends scheduling instances.", getSimulation().clockStr(), getName(), intraScheduler.getName());

            if (!statesManager.isInLatestPartitionSynGap(intraSchedulerResult.getScheduleTime())) {//把同步时对这一调度的记录补回来
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
        }
    }

    /**
     * Start the inter-scheduling process of an intra-scheduler.
     * Note that the inter-scheduling process is only started when the intra-scheduler is not busy and the queue is not empty.
     *
     * @param intraScheduler the intra-scheduler
     */
    private void startIntraScheduling(IntraScheduler intraScheduler) {
        if (intraScheduler.isQueuesEmpty()) {
            isIntraSchedulerBusy.put(intraScheduler, false);
        } else if (!intraScheduler.isQueuesEmpty()) {
            send(this, 0, CloudSimTag.INTRA_SCHEDULE_BEGIN, intraScheduler);
        }
    }

    /**
     * Call the {@link IntraScheduler} to allocate {@link Instance} to various hosts.
     * It will send {CloudSimTag#INTRA_SCHEDULE_END} to itself after the scheduling is completed.
     *
     * @param evt the event
     */
    private void processIntraScheduleBegin(SimEvent evt) {
        if (evt.getData() instanceof IntraScheduler intraScheduler) {
            IntraSchedulerResult innerScheduleResult = intraScheduler.schedule();

            double costTime = intraScheduler.getScheduleCostTime();

            LOGGER.info("{}: {}'s {} starts scheduling {} instances,cost {} ms", getSimulation().clockStr(), this.getName(), intraScheduler.getName(), innerScheduleResult.getInstanceNum(), costTime);

            send(this, costTime, CloudSimTag.INTRA_SCHEDULE_END, innerScheduleResult);
        }
    }

    /**
     * Handle the failed scheduling results of the {@link InterScheduler}.
     * Note that there is no check for scheduling timeout requests here.
     * @param instances the failed instances
     * @param intraScheduler the scheduler corresponding to this scheduling result
     * @param isNeedRevertSelfHostState whether to revert the state of the host
     */
    private void intraScheduleFailed(List<Instance> instances, IntraScheduler intraScheduler, boolean isNeedRevertSelfHostState) {
        Set<UserRequest> outDatedUserRequests = new HashSet<>();
        intraScheduleFailed(instances, intraScheduler, isNeedRevertSelfHostState, outDatedUserRequests);
    }

    /**
     * Handle the failed scheduling results of the {@link InterScheduler}.
     * The reason why the host state needs to be restored is that after each scheduler schedules an instance, it will default to a successful schedule, and then modify the host state it maintains.
     * Therefore, if the subsequently scheduled instance fails when handling the scheduling conflict, the host state needs to be reverted.
     * @param instances the failed instances
     * @param intraScheduler the scheduler corresponding to this scheduling result
     * @param isNeedRevertSelfHostState whether to revert the state of the host
     * @param outDatedUserRequests the outdated user requests that has exceeded the scheduling time limit
     */
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
            statesManager.revertSelfHostState(instances, intraScheduler);
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
     * @param evt the event
     */
    private void processLoadBalanceSend(SimEvent evt) {
        List<Instance> instances = instanceQueue.getAllItem();
        if (instances.size() != 0) {
            Set<IntraScheduler> sentIntraScheduler = loadBalancer.sendInstances(instances);
            if (instanceQueue.size() > 0) {
                send(this, loadBalancer.getLoadBalanceCostTime(), CloudSimTag.LOAD_BALANCE_SEND, null);
            }
            for (IntraScheduler intraScheduler : sentIntraScheduler) {
                if (!isIntraSchedulerBusy.containsKey(intraScheduler) || !isIntraSchedulerBusy.get(intraScheduler)) {
                    send(this, loadBalancer.getLoadBalanceCostTime(), CloudSimTag.INTRA_SCHEDULE_BEGIN, intraScheduler);
                    isIntraSchedulerBusy.put(intraScheduler, true);
                }
            }
        }
    }

    /**
     * Handle user requests that fail inter-scheduler scheduling.
     * This includes both scheduling failures due to insufficient resources and other reasons, and user requests that fail due to exceeding the scheduling time limit.
     *
     * @param evt the event
     */
    private void processScheduleToDcHostResponse(SimEvent evt) {
        if (evt.getTag() == CloudSimTag.SCHEDULE_TO_DC_HOST_CONFLICTED) {
            FailedOutdatedResult<InstanceGroup> failedOutdatedResult = (FailedOutdatedResult<InstanceGroup>) evt.getData();
            handleFailedInterScheduling(failedOutdatedResult.getFailRes(), failedOutdatedResult.getOutdatedRequests());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{}: {}'s {} failed to schedule {} instanceGroups,it need retry soon.", getSimulation().clockStr(), getName(), interScheduler.getName(), failedOutdatedResult.getFailRes().size());
            }
        }
    }

    // TODO This part of the logic needs to be checked

    /**
     * Processes the scheduling results of the cloud administrator's centralized inter-scheduler scheduling specific to the hosts in the data center.
     * @param evt the event
     */
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

    /**
     * Reverting the bandwidth occupied by an instance group that failed to schedule
     * @param instanceGroups the failed instance groups
     */
    private void revertBwForInstanceGroups(List<InstanceGroup> instanceGroups) {
        for (InstanceGroup instanceGroup : instanceGroups) {
            revertBwForInstanceGroup(instanceGroup);
            instanceGroup.setReceiveDatacenter(Datacenter.NULL);
        }
    }

    /**
     * Reverting the bandwidth occupied by an instance group that failed to schedule
     * @param instanceGroups the instance groups that have been successfully scheduled
     * @param failedInstanceGroups the failed instance groups
     * @param outDatedUserRequests  the failed user requests that has exceeded the scheduling time limit
     */
    private void revertBwForInstanceGroups(List<InstanceGroup> instanceGroups, List<InstanceGroup> failedInstanceGroups, Set<UserRequest> outDatedUserRequests) {
        for (InstanceGroup instanceGroup : failedInstanceGroups) {
            revertBwForInstanceGroup(instanceGroup);
            instanceGroup.setReceiveDatacenter(Datacenter.NULL);
        }

        //Revert the bandwidth occupied by the instance group that has been successfully scheduled but the user request has exceeded the scheduling time limit
        for (InstanceGroup instanceGroup : instanceGroups) {
            if (outDatedUserRequests.contains(instanceGroup.getUserRequest())) {
                revertBwForInstanceGroup(instanceGroup);
                instanceGroup.setReceiveDatacenter(Datacenter.NULL);
            }
        }
    }

    /**
     * Reverting the bandwidth occupied by an instance group that failed to schedule
     * @param instanceGroup the instance group
     */
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

    /**
     * Record the time when the instance group is received and the state of the instance group
     * @param instanceGroups the instance groups
     */
    private void markAndRecordInstanceGroups(List<InstanceGroup> instanceGroups) {
        instanceGroups.forEach(instanceGroup -> instanceGroup.setReceivedTime(getSimulation().clock()));
        instanceGroups.forEach(instanceGroup -> instanceGroup.setState(UserRequest.SCHEDULING));
        getSimulation().getSqlRecord().recordInstanceGroupsReceivedInfo(instanceGroups);
    }

    /**
     * Allocate bandwidth for the instance group
     * @param instanceGroups the instance groups
     * @return the instance groups which failed to allocate bandwidth
     */
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

    /**
     * Allocate resources for all instances in the instance groups
     * @param instanceGroups the instance groups
     * @return the instance groups which failed to allocate resources
     */
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

                int lifeTime = instance.getLifecycle();
                lifeInstancesMap.putIfAbsent(lifeTime, new ArrayList<>());
                lifeInstancesMap.get(lifeTime).add(instance);
            }
        }

        sendFinishInstanceRunEvt(lifeInstancesMap);

        return failedInstanceGroups;
    }

    /**
     * Update the state, the start time and the expected host of the instance after the instance is allocated
     * @param instance the instance
     */
    private void updateAfterInstanceAllocated(Instance instance) {
        instance.setState(UserRequest.RUNNING);
        instance.setStartTime(getSimulation().clock());
        instance.setHost(instance.getExpectedScheduleHostId());
        instance.setExpectedScheduleHostId(-1);
    }

    /**
     * Accepts relatives that cannot be forwarded to other data centers and adds all instances in the request to the queue
     * and attempts to start scheduling with the intra-scheduler.
     * @param evt the event
     */
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

    /**
     * Delete the failed instance group and set the received time of the instance group
     * @param instanceGroups the instance groups
     */
    private void deleteFailedInstanceGroupAndSetReceivedTime(List<InstanceGroup> instanceGroups) {
        Iterator<InstanceGroup> iterator = instanceGroups.iterator();
        while (iterator.hasNext()) {
            InstanceGroup instanceGroup = iterator.next();
            if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
                iterator.remove();
            } else {
                instanceGroup.setReceivedTime(getSimulation().clock());
            }
        }
    }

    /**
     * Allocate bandwidth for the instance group
     * @param instanceGroup the instance group
     * @param receiveDatacenter the data center that receives the instance group
     * @return whether the bandwidth allocation is successful
     */
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

    /**
     * When experimenting in a single data center scenario, user requests are accepted.
     * Note that since there is no inter-datacenter scheduling, all instances in user requests are directly put into the {@link InstanceQueue}
     * @param userRequests the user requests need to be accepted
     */
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

    /**
     * When experimenting in a multi-data center scenario, user requests are accepted.
     * Note that since there is inter-datacenter scheduling, all instances in user requests are directly put into the {@link InstanceGroupQueue}.
     * @param userRequestsTmp the user requests need to be accepted
     */
    private void acceptUserRequestForMultiDatacenters(List<?> userRequestsTmp){
        if (userRequestsTmp.get(0) instanceof UserRequest) {
            List<UserRequest> userRequests = (List<UserRequest>) userRequestsTmp;

            interScheduler.addUserRequests(userRequests);
        } else if (userRequestsTmp.get(0) instanceof InstanceGroup) {
            List<InstanceGroup> instanceGroups = (List<InstanceGroup>) userRequestsTmp;

            interScheduler.addInstanceGroups(instanceGroups, false);
        }

        if (!isInterSchedulerBusy) {
            sendNow(this, CloudSimTag.INTER_SCHEDULE_BEGIN);
            isInterSchedulerBusy = true;//放在这里可以防止同一时间多次触发
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

    /**
     * Determine if the scheduling result of the data center itself is empty
     * @param interSchedulerResult the result of the inter-scheduler
     * @return whether the scheduling result of the data center itself is empty
     */
    private boolean isScheduleToSelfEmpty(InterSchedulerResult interSchedulerResult) {
        Map<Datacenter, List<InstanceGroup>> scheduledResultMap = interSchedulerResult.getScheduledResultMap();
        return !scheduledResultMap.containsKey(this) || scheduledResultMap.get(this).size() == 0;
    }

    /**
     * Try to start the inter-scheduling process if the queue is not empty.
     */
    private void startInterScheduling() {
        if (interScheduler.isQueuesEmpty()) {
            isInterSchedulerBusy = false;
        } else {
            send(this, 0, CloudSimTag.INTER_SCHEDULE_BEGIN, null);
        }
    }

    /**
     * Send scheduling results of the inter-scheduler to the corresponding data center.
     * @param interSchedulerResult the result of the inter-scheduler
     */
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

    /**
     * Add the data center to the forward history of the instance group
     * @param instanceGroups the instance groups
     */
    private void addSelfInForwardHistory(List<InstanceGroup> instanceGroups) {
        instanceGroups.forEach(instanceGroup -> instanceGroup.addForwardDatacenterIdHistory(getId()));
    }

    /**
     * Handle the failed scheduling results of the inter-scheduler.
     * @param failedInstanceGroups the failed instance groups
     * @param outDatedUserRequests the failed user requests that has exceeded the scheduling time limit
     */
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

    /**
     * Handle the failed scheduling results of the inter-scheduler.
     * Noted that there is no check for scheduling timeout requests here.
     * @param failedInstanceGroups the failed instance groups
     */
    private void handleFailedInterScheduling(List<InstanceGroup> failedInstanceGroups) {
        Set<UserRequest> outDatedUserRequests = new HashSet<>();
        handleFailedInterScheduling(failedInstanceGroups, outDatedUserRequests);
    }

    @Override
    public boolean isCentralizedInterSchedule() {
        return centralizedInterScheduleFlag;
    }

    @Override
    public double getEstimatedTCO(InstanceGroup instanceGroup) {
        double tco = 0;
        for (Instance instance : instanceGroup.getInstances()) {
            tco += instance.getCpu() * pricePerCpuPerSec * instance.getLifecycle() / 1000.0
                    + instance.getRam() * pricePerRamPerSec * instance.getLifecycle() / 1000.0
                    + instance.getStorage() * pricePerStoragePerSec * instance.getLifecycle() / 1000.0
                    + instance.getBw() * pricePerBwPerSec * instance.getLifecycle() / 1000.0
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
}
