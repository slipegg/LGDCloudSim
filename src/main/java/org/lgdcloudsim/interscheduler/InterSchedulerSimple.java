package org.lgdcloudsim.interscheduler;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.queue.QueueResult;
import org.lgdcloudsim.network.NetworkTopology;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.queue.InstanceGroupQueue;
import org.lgdcloudsim.queue.InstanceGroupQueueFifo;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.statemanager.DetailedDcStateSimple;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.SimpleStateEasyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The InterSchedulerSimple class is an implementation of the {@link InterScheduler} interface.
 * <p>
 * The schedule algorithm as follows:
 * <ul>
 *      <li>scheduleToDatacenter algorithm: Randomly select one of all data centers that may have sufficient resources.
 *      If the sum of the remaining resources in all data centers does not meet the resources required by the instance group, the instance group scheduling fails.</li>
 *      <li>scheduleToHost algorithm: Randomly select a host from all data centers that may have sufficient resources and start traversing until a suitable host is found.
 *      If all hosts are traversed and there is still no suitable one, the scheduling of this instance group fails.</li>
 *      <li>scheduleMixed algorithm: It will first try to schedule all instance in the instance group to the hosts in the data center where the inter-scheduler is located.
 *      The schedule algorithm is that randomly select a host from all hosts in the data center and start traversing until a suitable host is found.
 *      If there is no suitable host in the data center, it will try to forward the instance group to other data centers.
 *      The schedule algorithm is that randomly forward the instance group to one of the data centers that may have sufficient resources.
 *      </li>
 * </ul>
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InterSchedulerSimple implements InterScheduler {
    /**
     * The scheduling target of the inter-scheduler.
     * The initial value is NULL, it needs to be set to DC_TARGET, HOST_TARGET, or MIXED_TARGET.
     * You also can set it to other values, but you need to implement the corresponding scheduling algorithm.
     */
    public static final int NULL = -1;

    /**
     * The scheduling target of the inter-scheduler is the data center.
     */
    public static final int DC_TARGET = 0;

    /**
     * The scheduling target of the inter-scheduler is the host.
     */
    public static final int HOST_TARGET = 1;

    /**
     * The scheduling target of the inter-scheduler is the mix of hosts and data centers.
     */
    public static final int MIXED_TARGET = 2;

    /**
     * The logger of the inter-scheduler.
     */
    public Logger LOGGER = LoggerFactory.getLogger(InterSchedulerSimple.class.getName());

    /**
     * The target of the inter-scheduler.
     * It can be set to {@link InterSchedulerSimple#HOST_TARGET}, {@link InterSchedulerSimple#DC_TARGET} or {@link InterSchedulerSimple#MIXED_TARGET}.
     */
    @Getter
    int target;

    /**
     * It is used when the target is {@link InterSchedulerSimple#DC_TARGET} or {@link InterSchedulerSimple#MIXED_TARGET}.
     * It is a flag to set whether the instance group is allowed to be forwarded to other data centers after being sent to the target data center.
     * Note that it is only used when inter-scheduler schedules instance groups to data centers but not schedules the instance of the instance group to hosts.
     */
    @Getter
    boolean isSupportForward;

    /**
     * The simulation.
     */
    @Getter
    @Setter
    Simulation simulation;

    /**
     * The collaboration id.
     * It is used for the centralized inter-scheduler of the collaboration zone in CIS{@link org.lgdcloudsim.core.CloudInformationService}.
     */
    @Getter
    @Setter
    int collaborationId;

    /**
     * The data center where the inter-scheduler is located.
     * If the inter-scheduler is a centralized inter-scheduler of the collaboration zone in CIS{@link org.lgdcloudsim.core.CloudInformationService},
     * it will not have a data center where it is located.
     */
    @Getter
    Datacenter datacenter;

    /**
     * The name of the inter-scheduler.
     */
    @Getter
    String name;

    /**
     * The id of the inter-scheduler.
     */
    @Getter
    int id;

    /**
     * The instance group queue that stores the new instance groups waiting for scheduling.
     */
    @Getter
    InstanceGroupQueue instanceGroupQueue = new InstanceGroupQueueFifo();

    /**
     * The instance group queue that stores the instance groups that need to be retried for scheduling.
     * When a new round of scheduling starts,
     * the instance group will always be fetched from the retryInstanceGroupQueue first.
     * If the number is not enough, the instance group will be fetched from the general instanceGroupQueue.
     */
    @Getter
    InstanceGroupQueue retryInstanceGroupQueue = new InstanceGroupQueueFifo();

    /**
     * The state synchronization interval of each data center.
     */
    @Getter
    @Setter
    Map<Datacenter, Double> dcStateSynInterval = new HashMap<>();

    /**
     * The state synchronization type of each data center.
     */
    @Getter
    @Setter
    private Map<Datacenter, String> dcStateSynType = new HashMap<>();

    /**
     * The simple state get from data center by synchronization.
     */
    @Getter
    Map<Datacenter, Object> interScheduleSimpleStateMap = new HashMap<>();

    /**
     * The random object.
     */
    Random random = new Random();

    /**
     * The time spent on the scheduling.
     */
    @Getter
    double scheduleTime = 0.0;

    /**
     * The number of traversals in the schedule
     */
    @Getter
    int traversalTime = 0;

    /**
     * The constructor of the InterSchedulerSimple class.
     * @param id the id of the inter-scheduler
     * @param simulation the simulation
     * @param collaborationId the collaboration zone id
     * @param target the target of the inter-scheduler, it can be set to {@link InterSchedulerSimple#HOST_TARGET}, {@link InterSchedulerSimple#DC_TARGET} or {@link InterSchedulerSimple#MIXED_TARGET}
     * @param isSupportForward whether the scheduled instance group by the inter-scheduler supports forward
     */
    public InterSchedulerSimple(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        this.id = id;
        this.name = "collaboration" + collaborationId + "-InterScheduler" + id;
        this.simulation = simulation;
        this.collaborationId = collaborationId;
        this.target = target;
        this.isSupportForward = isSupportForward;
    }

    /**
     * Filter to obtain the appropriate data center based on the network constraints of the instance group.
     * It needs to consider the access delay of the instance group,
     * the connection delay between instance groups, and the bandwidth required to be rented between instances.
     *
     * @param instanceGroups the instance groups
     * @return the instance group and the list of available data centers
     */
    protected Map<InstanceGroup, List<Datacenter>> filterSuitableDatacenterByNetwork(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        NetworkTopology networkTopology = simulation.getNetworkTopology();
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = new HashMap<>();
        for (InstanceGroup instanceGroup : instanceGroups) {
            List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);

            filterDatacentersByAccessLatency(instanceGroup, availableDatacenters, networkTopology);
            instanceGroupAvailableDatacenters.put(instanceGroup, availableDatacenters);
        }
        interScheduleByNetworkTopology(instanceGroupAvailableDatacenters, networkTopology);
        return instanceGroupAvailableDatacenters;
    }

    /**
     * Schedule the instance groups.
     * First, it will try to synchronize the state of the data center which is synchronized in real time.
     * Then, it will try to get the instance groups that need to be scheduled from the two instance group queues.
     * In the end, it will try to call the corresponding scheduling algorithm according to the target of the inter-scheduler.
     *
     * @return the result of the inter-scheduler
     */
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

        setInstanceGroupInterScheduleEndTime(waitSchedulingInstanceGroups, getSimulation().clock() + this.scheduleTime);

        interSchedulerResult = checkInstanceGroupScheduleResult(interSchedulerResult);

        if(end-start<0.1) {
            LOGGER.debug("{}: interSchedule schedule time is less than 0.1 ms ({} ms).", simulation.clockStr(), end - start);
        }

        interSchedulerResult.setOutDatedUserRequests(queueResult.getOutDatedItems());
        return interSchedulerResult;
    }

    private InterSchedulerResult checkInstanceGroupScheduleResult(InterSchedulerResult interSchedulerResult) {
        InterSchedulerResult interSchedulerResultTmp = new InterSchedulerResult(this, simulation.getCollaborationManager().getDatacenters(collaborationId));
        for(InstanceGroup failedInstanceGroup: interSchedulerResult.getFailedInstanceGroups()){
            interSchedulerResultTmp.addFailedInstanceGroup(failedInstanceGroup);
        }
        for(Map.Entry<Datacenter, List<InstanceGroup>> dcResultEntry: interSchedulerResult.getScheduledResultMap().entrySet()) {
            Datacenter datacenter = dcResultEntry.getKey();
            for(InstanceGroup instanceGroup: dcResultEntry.getValue()){
                if(checkInstanceGroupLimit(instanceGroup, datacenter, simulation.getNetworkTopology(), interSchedulerResult)){
                    interSchedulerResultTmp.addDcResult(instanceGroup, datacenter);
                } else {
                    interSchedulerResultTmp.addFailedInstanceGroup(instanceGroup);
                    instanceGroup.getUserRequest().addFailReason("network topology constraints");
                }
            }
        }
        return interSchedulerResultTmp;
    }

    private boolean checkInstanceGroupLimit(InstanceGroup instanceGroup, Datacenter datacenter, NetworkTopology networkTopology, InterSchedulerResult interSchedulerResult) {
        // 检查单点约束是否满足
        if (instanceGroup.getAccessLatency() < networkTopology.getAccessLatency(instanceGroup.getUserRequest(), datacenter)) {
            return false;
        }
        // 检查拓扑约束是否满足
        for (InstanceGroup dstInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getDstList(instanceGroup)) {
            Datacenter scheduledDatacenter = getPossibleScheduledDatacenter(dstInstanceGroup, interSchedulerResult);
            if (scheduledDatacenter != Datacenter.NULL) {
                if(networkTopology.getDelay(datacenter, scheduledDatacenter) > instanceGroup.getUserRequest().getInstanceGroupGraph().getDelay(instanceGroup, dstInstanceGroup)) {
                    return false;
                }
            }
        }
        for (InstanceGroup srcInstanceGroup : instanceGroup.getUserRequest().getInstanceGroupGraph().getSrcList(instanceGroup)) {
            Datacenter scheduledDatacenter = getPossibleScheduledDatacenter(srcInstanceGroup, interSchedulerResult);
            if (scheduledDatacenter != Datacenter.NULL) {
                if(networkTopology.getDelay(datacenter, scheduledDatacenter) > instanceGroup.getUserRequest().getInstanceGroupGraph().getDelay(srcInstanceGroup, instanceGroup)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setInstanceGroupInterScheduleEndTime(List<InstanceGroup> waitSchedulingInstanceGroups, double scheduleEndTime) {
        for (InstanceGroup instanceGroup : waitSchedulingInstanceGroups) {
            instanceGroup.setInterScheduleEndTime(scheduleEndTime);
        }
    }

    /**
     * It will first try to schedule all instance in the instance group to the hosts in the data center where the inter-scheduler is located.
     * The schedule algorithm is that randomly select a host from all hosts in the data center and start traversing until a suitable host is found.
     * If there is no suitable host in the data center, it will try to forward the instance group to other data centers.
     * The schedule algorithm is that randomly forward the instance group to one of the data centers that may have sufficient resources.
     * @param instanceGroups the instance groups to be scheduled
     * @return the result of the inter-scheduler
     */
    protected InterSchedulerResult scheduleMixed(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(this, allDatacenters);
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = filterSuitableDatacenterByNetwork(instanceGroups);

        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleResEntry : instanceGroupAvailableDatacenters.entrySet()) {
            InstanceGroup instanceGroupToBeScheduled = scheduleResEntry.getKey();
            List<Datacenter> availableDatacenters = scheduleResEntry.getValue();

            if (availableDatacenters.isEmpty()) {
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

    /**
     * Try to schedule all instance in a instance group to the hosts in the data center where the inter-scheduler is located.
     * If any instance in the instance group is not scheduled successfully, it will try to forward the instance group to other data centers.
     * @param instanceGroup the instance group to be scheduled
     * @param availableDatacenters the available data centers
     * @return the data center where the instance group is scheduled
     */
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

    /**
     * Select an available data center to forward the instance group randomly.
     * @param instanceGroup the instance group to be scheduled
     * @param availableDatacenters the available data centers
     * @return the data center where the instance group is scheduled
     */
    private Datacenter selectDcToForward(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters) {
        if (availableDatacenters.isEmpty()) {
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

    /**
     * Get the instance groups that need to be scheduled from the instance group queues.
     * @return the result of the instance group queues
     */
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

    /**
     * Synchronize the state of the data center which is synchronized in real time.
     */
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

    /**
     * Randomly select one of all data centers that may have sufficient resources.
     * If the sum of the remaining resources in all data centers does not meet the resources required by the instance group,
     * the instance group scheduling fails.
     * @param instanceGroups the instance groups to be scheduled
     * @return the result of the inter-scheduler
     */
    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(this, allDatacenters);
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = filterSuitableDatacenterByNetwork(instanceGroups);

        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleRes : instanceGroupAvailableDatacenters.entrySet()) {
            if (scheduleRes.getValue().isEmpty()) {
                interSchedulerResult.getFailedInstanceGroups().add(scheduleRes.getKey());
            } else {
                Datacenter target = scheduleRes.getValue().get(random.nextInt(scheduleRes.getValue().size()));
                interSchedulerResult.addDcResult(scheduleRes.getKey(), target);
            }
        }

        return interSchedulerResult;
    }

    /**
     * Randomly select one of all data centers that may have sufficient resources.
     * If the sum of the remaining resources in all data centers does not meet the resources required by the instance group, the instance group scheduling fails.
     * @param instanceGroups the instance groups to be scheduled
     * @return the result of the inter-scheduler
     */
    protected InterSchedulerResult scheduleToHost(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(this, allDatacenters);

        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = filterSuitableDatacenterByNetwork(instanceGroups);

        for (Map.Entry<InstanceGroup, List<Datacenter>> scheduleResEntry : instanceGroupAvailableDatacenters.entrySet()) {
            InstanceGroup instanceGroupToBeScheduled = scheduleResEntry.getKey();
            List<Datacenter> availableDatacenters = scheduleResEntry.getValue();
            if (availableDatacenters.isEmpty()) {
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

    /**
     * Schedule all instance in the instance group to the hosts in the available data centers.
     * @param instanceGroup the instance group to be scheduled
     * @param availableDatacenters the available data centers
     * @return the data center where the instance group is scheduled
     */
    protected Datacenter scheduleForInstanceGroupAndInstance(InstanceGroup instanceGroup, List<Datacenter> availableDatacenters) {
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

    /**
     * Get the data center id by the host index in all available data centers.
     * @param hostIdInAll the host index in all available data centers
     * @param availableDatacenters the available data centers
     * @return the data center id
     */
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

    /**
     * Schedule all instances of the instance group to the hosts in the data center.
     * @param instanceGroup the instance group to be scheduled
     * @param datacenter the data center
     * @return whether the scheduling is successful
     */
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

    /**
     * Randomly schedule the instance to the hosts in the data center.
     * @param instance the instance to be scheduled
     * @param detailedDcStateSimple the state of the data center, See {@link DetailedDcStateSimple}
     * @return the host id where the instance is scheduled
     */
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

    /**
     * Record the scheduled result in the instances.
     * @param scheduleResult the scheduled result
     */
    private void recordScheduledResultInInstances(Map<Instance, Integer> scheduleResult) {
        for (Map.Entry<Instance, Integer> scheduleResultEntry : scheduleResult.entrySet()) {
            Instance instance = scheduleResultEntry.getKey();
            int hostId = scheduleResultEntry.getValue();

            instance.setExpectedScheduleHostId(hostId);
        }
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
        return instanceGroupQueue.isEmpty() && retryInstanceGroupQueue.isEmpty();
    }

    @Override
    public int getNewQueueSize() {
        return instanceGroupQueue.size();
    }

    @Override
    public int getRetryQueueSize() {
        return retryInstanceGroupQueue.size();
    }

    /**
     * Get the available data centers for the instance group by the network limitations and the simple state.
     * @param instanceGroup the instance group
     * @param allDatacenters all data centers
     * @param networkTopology the network topology
     * @return the available data centers
     */
    List<Datacenter> getAvailableDatacenters(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        List<Datacenter> availableDatacenters = new ArrayList<>(allDatacenters);
        //Filter available data centers based on access delay requirements
        filterDatacentersByAccessLatency(instanceGroup, availableDatacenters, networkTopology);
        //Filter available data centers based on the simple state.
        filterDatacentersByResourceSample(instanceGroup, availableDatacenters);
        return availableDatacenters;
    }

    /**
     * Filter the data centers based on the access latency.
     * @param instanceGroup the instance group
     * @param allDatacenters all data centers
     * @param networkTopology the network topology
     */
    private void filterDatacentersByAccessLatency(InstanceGroup instanceGroup, List<Datacenter> allDatacenters, NetworkTopology networkTopology) {
        // Filter based on access latency
        allDatacenters.removeIf(
                datacenter -> instanceGroup.getAccessLatency() <= networkTopology.getAccessLatency(instanceGroup.getUserRequest(), datacenter));
    }

    /**
     * Filter the data centers based on the simple state.
     * @param instanceGroup the instance group
     * @param allDatacenters all data centers
     */
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
    }

    /**
     * Filter the data centers based on the network topology.
     *
     * @param instanceGroupAvailableDatacenters the instance group and the list of available data centers
     * @param networkTopology                   the network topology
     */
    void interScheduleByNetworkTopology(Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters, NetworkTopology networkTopology) {
        //TODO Filter according to the delay and bandwidth in the network topology to obtain the optimal scheduling plan
        //TODO Later, a backtracking algorithm can be added for simple screening.
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
        this.name = "Datacenter" + datacenter.getId() + "-InterScheduler" + id;
    }

    /**
     * Get the possible scheduled data center from the previous scheduling result.
     * @param instanceGroup the instance group.
     * @param interSchedulerResult the result of the scheduling.
     * @return the possible scheduled data center.
     */
    private static Datacenter getPossibleScheduledDatacenter(InstanceGroup instanceGroup, InterSchedulerResult interSchedulerResult) {
        if (instanceGroup.getReceiveDatacenter() != Datacenter.NULL) {
            return instanceGroup.getReceiveDatacenter();
        } else {
            return interSchedulerResult.getScheduledDatacenter(instanceGroup);
        }
    }

}
