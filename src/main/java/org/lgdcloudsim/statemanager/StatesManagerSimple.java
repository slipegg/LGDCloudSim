package org.lgdcloudsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.datacenter.DatacenterPowerOnRecord;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.intrascheduler.IntraSchedulerResult;
import org.lgdcloudsim.request.Instance;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;

/**
 * A class to manage the states of datacenter.
 * This class implements the interface {@link StatesManager}.
 * There we will introduce the methods to manage the states of the datacenter.
 * Because the intra-scheduler necessitates synchronization to acquire the host state in each partition in the data center.
 * However, using a simple method of duplicating all states to the intra-scheduler during each synchronization would result in redundant state copies and substantial memory wastage.
 * As a result, we optimize the maintenance of historical host resource states through a multi-level incremental state representation (MLI) method.
 * The core idea of this method is only to perform appropriate state replication on changed hosts to avoid redundant replication.
 * When synchronizing the host states to the intra-scheduler, the system creates an empty hash table in synStateMap for each partition and deletes outdated historical hash tables.
 * Before the next synchronization, if a host's state changes, the host state before the change is put into the new hash table of the corresponding partition to prevent state loss.
 * Since these hash tables synStateMap maintained by the state manager do not belong exclusively to an intra-scheduler, these tables are read-only to the intra-scheduler.
 * Therefore, each intra-scheduler also needs to maintain additional hash tables selfHostStateMap for each partition.
 * The hash tables document the state of the scheduled hosts from the intra-scheduler's own view before the next synchronization.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class StatesManagerSimple implements StatesManager {
    /**
     * The actual status of all hosts at the current time
     **/
    private int[] actualHostStates;

    /**
     * The status of all hosts maintained by the center state manager at the current time,
     * such as etcd in k8s.
     * When the heartbeatInterval <= 0, the actual host status is always synchronized with the center status,
     * so we don't need to initialize the centerHostStates, we just use the actualHostStates as the centerHostStates.
     **/
    private int[] centerHostStates;

    /**
     * The interval of heartbeat synchronization.
     * If heartbeatInterval <=0,it means that we do not use the heartbeat reporting mechanism,
     * that is, the actual host status is always synchronized with the center status.
     * The unit is ms.
     * The default value is 0.
     */
    private int heartbeatInterval;

    /**
     * The time when the first heartbeat is sent during a heartbeat interval.
     * The host id is the index of the array.
     * The send time is the value of the array.
     * Note that if the heartbeatInterval <= 0, the heartbeatSendTime is not used.
     * The unit is ms.
     */
    private int[] heartbeatSendTime;

    /**
     * A random number generator of heartbeatSendTime
     **/
    private Random random;
    /**
     * Whether to enable prediction
     **/
    private boolean predictable;

    /**
     * The host state at the time of synchronization
     **/
    private Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap;

    /**
     * Maintain separate selfHostState for each scheduler
     **/
    private Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap;

    /**
     * Number of partition in the datacenter
     **/
    private int partitionNum;

    /**
     * Number of hosts in the datacenter
     **/
    @Getter
    private int hostNum;

    /**
     * see {@link  DatacenterPowerOnRecord}
     **/
    @Getter
    private DatacenterPowerOnRecord datacenterPowerOnRecord;

    /**
     * see {@link  PartitionRangesManager}
     **/
    @Getter
    private PartitionRangesManager partitionRangesManager;

    /**
     * see {@link  PredictionManager}
     **/
    @Getter
    @Setter
    private PredictionManager predictionManager;

    /**
     * The record data num for predicting
     **/
    @Getter
    @Setter
    private int predictRecordNum = 1;

    /**
     * The datacenter,see {@link Datacenter}
     **/
    @Getter
    @Setter
    private Datacenter datacenter;

    /**
     * see {@link  SimpleState}
     **/
    @Getter
    @Setter
    //TODO 加入心跳后处理时机需要多多考虑
    private SimpleState simpleState;

    /**
     * The max cpu capacity among all hosts in the datacenter
     */
    @Getter
    private int maxCpuCapacity;

    /**
     * The max ram capacity among all hosts in the datacenter
     */
    @Getter
    private int maxRamCapacity;

    /**
     * see {@link  SynGapManager}
     */
    private SynGapManager synGapManager;

    /**
     * see {@link  HostCapacityManager}
     */
    @Getter
    private HostCapacityManager hostCapacityManager;

    /**
     * The view of each intra-scheduler.
     */
    private Map<IntraScheduler, List<Integer>> intraSchedulerView;

    /**
     * Initialize the StatesManagerSimple with a random seed.
     *
     * @param hostNum                the number of hosts in the datacenter.
     * @param partitionRangesManager the partition ranges manager.
     * @param synGap                 the synchronization gap.
     * @param heartbeatInterval      the interval of heartbeat synchronization.
     * @param maxCpuCapacity         the max cpu capacity among all hosts in the datacenter.
     * @param maxRamCapacity         the max ram capacity among all hosts in the datacenter.
     * @param randomSeed             the random seed.
     */
    public StatesManagerSimple(int hostNum, PartitionRangesManager partitionRangesManager, double synGap, int heartbeatInterval, int maxCpuCapacity, int maxRamCapacity, int randomSeed) {
        this(hostNum, partitionRangesManager, synGap, heartbeatInterval, maxCpuCapacity, maxRamCapacity);
        random = new Random(randomSeed);
    }

    /**
     * Initialize the StatesManagerSimple.
     *
     * @param hostNum                the number of hosts in the datacenter.
     * @param partitionRangesManager the partition ranges manager.
     * @param synGap                 the synchronization gap.
     * @param heartbeatInterval      the interval of heartbeat synchronization.
     * @param maxCpuCapacity         the max cpu capacity among all hosts in the datacenter.
     * @param maxRamCapacity         the max ram capacity among all hosts in the datacenter.
     */
    public StatesManagerSimple(int hostNum, PartitionRangesManager partitionRangesManager, double synGap, int heartbeatInterval, int maxCpuCapacity, int maxRamCapacity) {
        this.hostNum = hostNum;
        this.heartbeatInterval = heartbeatInterval;
        this.partitionRangesManager = partitionRangesManager;
        this.synGapManager = new SynGapManager(synGap, partitionRangesManager.getPartitionNum());
        this.maxCpuCapacity = maxCpuCapacity;
        this.maxRamCapacity = maxRamCapacity;
        this.actualHostStates = new int[hostNum * HostState.STATE_NUM];
        if (isNeedHeartbeat()) {
            this.centerHostStates = new int[hostNum * HostState.STATE_NUM];
            this.heartbeatSendTime = new int[hostNum];
            random = new Random();
            initHeartbeatSendTime();
        }
        this.simpleState = new SimpleStateEasy(this);
        this.partitionNum = partitionRangesManager.getPartitionNum();
        this.selfHostStateMap = new HashMap<>();
        this.datacenterPowerOnRecord = new DatacenterPowerOnRecord();
        this.hostCapacityManager = new HostCapacityManager();
        this.intraSchedulerView = new HashMap<>();
        initSynStateMap();
    }

    /**
     * Initialize the StatesManagerSimple.
     *
     * @param hostNum                the number of hosts in the datacenter.
     * @param partitionRangesManager the partition ranges manager.
     * @param synGap                 the synchronization gap.
     * @param maxCpuCapacity         the max cpu capacity among all hosts in the datacenter.
     * @param maxRamCapacity         the max ram capacity among all hosts in the datacenter.
     */
    public StatesManagerSimple(int hostNum, PartitionRangesManager partitionRangesManager, double synGap, int maxCpuCapacity, int maxRamCapacity) {
        this(hostNum, partitionRangesManager, synGap, 0, maxCpuCapacity, maxRamCapacity);
    }

    /**
     * Initialize the StatesManagerSimple.
     * @param hostNum the number of hosts in the datacenter.
     * @param partitionRangesManager the partition ranges manager.
     * @param synGap the synchronization gap.
     */
    public StatesManagerSimple(int hostNum, PartitionRangesManager partitionRangesManager, double synGap) {
        this(hostNum, partitionRangesManager, synGap, 128, 256);
    }

    private void initHeartbeatSendTime() {
        for (int i = 0; i < hostNum; i++) {
            int heartBeatSendTime = random.nextInt(heartbeatInterval);
            heartbeatSendTime[i] = heartBeatSendTime;
        }
    }

    /**
     * Initialize the host states of the datacenter.
     *
     * @param cpu     the cpu capacity of the host.
     * @param ram     the ram capacity of the host.
     * @param storage the storage capacity of the host.
     * @param bw      the bw capacity of the host.
     * @param startId the start id of the host.
     * @param length  the number of hosts that has the same capacity.
     * @return the host state.
     */
    //TODO: The initHostStates function does not need to be in order,
    // but here it is required that hostCapacityManager is initialized in order,
    // which needs to be modified later.But since this is only used in initDatacenter, there is no problem yet.
    @Override
    public StatesManager initHostStates(int cpu, int ram, int storage, int bw, int startId, int length) {
        int endId = startId + length - 1;
        for (int i = startId; i <= endId; i++) {
            initSingleHostState(i, cpu, ram, storage, bw);
        }
        hostCapacityManager.orderlyAddSameCapacityHost(length, new int[]{cpu, ram, storage, bw});
        return this;
    }

    /**
     * Initialize the host states of the datacenter by {@link HostStateGenerator}.
     * @param hostStateGenerator the host state generator.
     * @return the host state.
     */
    @Override
    public StatesManager initHostStates(HostStateGenerator hostStateGenerator) {
        for (int i = 0; i < hostNum; i++) {
            int[] state = hostStateGenerator.generateHostState();
            initSingleHostState(i, state);
        }
        return this;
    }

    /**
     * Initialize the host state with the given state.
     * @param hostId the id of the host.
     * @param state the state of the host.
     */
    private void initSingleHostState(int hostId, int[] state) {
        if (state.length != HostState.STATE_NUM) {
            throw new IllegalArgumentException("Host state must be array of size " + HostState.STATE_NUM);
        }
        System.arraycopy(state, 0, actualHostStates, hostId * HostState.STATE_NUM, HostState.STATE_NUM);
        if (isNeedHeartbeat()) {
            System.arraycopy(state, 0, centerHostStates, hostId * HostState.STATE_NUM, HostState.STATE_NUM);
        }
        simpleState.initHostSimpleState(hostId, state);
    }

    /**
     * Initialize the host state with the given state.
     * @param hostId the id of the host.
     * @param cpu the cpu capacity of the host.
     * @param ram the ram capacity of the host.
     * @param storage the storage capacity of the host.
     * @param bw the bw capacity of the host.
     */
    private void initSingleHostState(int hostId, int cpu, int ram, int storage, int bw) {
        initSingleHostState(hostId, new int[]{cpu, ram, storage, bw});
    }

    /**
     * In order to save space, heartbeat are only made when the host status changes.
     * We need to synchronize the status of these changed hosts from actualHostStates to centerHostStates
     *
     * @param updatedHostIds the ids of the hosts that have changed and make heartbeat
     * @return the StatesManager itself.
     */
    @Override
    public StatesManager synByHeartbeat(List<Integer> updatedHostIds) {
        if (!isNeedHeartbeat()) {
            return this;
        }
        for (int hostId : updatedHostIds) {
            System.arraycopy(actualHostStates, hostId * HostState.STATE_NUM, centerHostStates, hostId * HostState.STATE_NUM, HostState.STATE_NUM);
        }
        return this;
    }

    /**
     * Get the host state of the host with hostId in the intra-scheduler's view.
     * @see SynState
     * @param scheduler the intra-scheduler.
     * @return the host state.
     */
    @Override
    public SynState getSynStateForIntraScheduler(IntraScheduler scheduler) {
        Map<Integer, Map<Integer, int[]>> selfHostState;
        int[] partitionIds = partitionRangesManager.getPartitionIds();

        if (!selfHostStateMap.containsKey(scheduler) || !synGapManager.isSynCostTime()) {
            selfHostStateMap.put(scheduler, new HashMap<>());
            for (int partitionId : partitionIds) {
                selfHostStateMap.get(scheduler).put(partitionId, new HashMap<>());
            }
        }
        selfHostState = selfHostStateMap.get(scheduler);
        return new SynStateSimple(synStateMap, getCenterHostStates(), partitionRangesManager, selfHostState, scheduler, predictionManager, synGapManager, predictRecordNum, predictable);
    }

    /**
     * Perform a partition synchronization for each {@link IntraScheduler}.
     */
    @Override
    public StatesManager synAllStateBetweenCenterAndIntraScheduler() {
        if (!isSynCostTime()) {
            return this;
        }
        synGapManager.partitionSynGapCountAddOne();
        int latestSmallSynGapCount = synGapManager.getPartitionSynCount();
        for (Map<Double, Map<Integer, int[]>> partitionSynStateMap : synStateMap.values()) {
            if (!predictable && latestSmallSynGapCount >= partitionNum) {
                partitionSynStateMap.remove(synGapManager.getSynTime(latestSmallSynGapCount - partitionNum));
            } else if (predictable && latestSmallSynGapCount >= partitionNum * getPredictRecordNum()) {
                partitionSynStateMap.remove(synGapManager.getSynTime(latestSmallSynGapCount - partitionNum * getPredictRecordNum()));
            }
            partitionSynStateMap.put(synGapManager.getSynTime(latestSmallSynGapCount), new HashMap<>());
        }
        for (IntraScheduler scheduler : selfHostStateMap.keySet()) {
            int clearPartitionId = (latestSmallSynGapCount + scheduler.getFirstPartitionId()) % partitionNum;
            selfHostStateMap.get(scheduler).get(clearPartitionId).clear();
        }
        return this;
    }

    /**
     * Allocate the instance to the host.
     * @param hostId the id of the host.
     * @param instance the instance.
     * @return whether the instance is allocated to the host successfully.
    */
    @Override
    public boolean allocate(int hostId, Instance instance) {
        int[] beforeHostState = new int[HostState.STATE_NUM];
        System.arraycopy(getCenterHostStates(), hostId * HostState.STATE_NUM, beforeHostState, 0, HostState.STATE_NUM);
        if (beforeHostState[0] < instance.getCpu() || beforeHostState[1] < instance.getRam() || beforeHostState[2] < instance.getStorage() || beforeHostState[3] < instance.getBw()
                || actualHostStates[hostId * HostState.STATE_NUM] < instance.getCpu() || actualHostStates[hostId * HostState.STATE_NUM + 1] < instance.getRam()
                || actualHostStates[hostId * HostState.STATE_NUM + 2] < instance.getStorage() || actualHostStates[hostId * HostState.STATE_NUM + 3] < instance.getBw()) {
            return false; //This usually doesn't happen because the previous conflict handler has already checked it.
        }

        updateSynStateMap(hostId, beforeHostState);

        actualHostStates[hostId * HostState.STATE_NUM] -= instance.getCpu();
        actualHostStates[hostId * HostState.STATE_NUM + 1] -= instance.getRam();
        actualHostStates[hostId * HostState.STATE_NUM + 2] -= instance.getStorage();
        actualHostStates[hostId * HostState.STATE_NUM + 3] -= instance.getBw();

        if (isNeedHeartbeat()) {
            centerHostStates[hostId * HostState.STATE_NUM] -= instance.getCpu();
            centerHostStates[hostId * HostState.STATE_NUM + 1] -= instance.getRam();
            centerHostStates[hostId * HostState.STATE_NUM + 2] -= instance.getStorage();
            centerHostStates[hostId * HostState.STATE_NUM + 3] -= instance.getBw();
        }

        simpleState.updateSimpleStateAllocated(hostId, beforeHostState, instance);
        datacenterPowerOnRecord.hostAllocateInstance(hostId, datacenter.getSimulation().clock());
        return true;
    }

    /**
     * Release the instance from the host.
     * @param hostId the id of the host.
     * @param instance the instance to be released.
     * @return the StatesManager itself.
     */
    @Override
    public StatesManager release(int hostId, Instance instance) {
        int[] beforeHostState = new int[HostState.STATE_NUM];
        System.arraycopy(actualHostStates, hostId * HostState.STATE_NUM, beforeHostState, 0, HostState.STATE_NUM);

        updateSynStateMap(hostId, beforeHostState);

        actualHostStates[hostId * HostState.STATE_NUM] += instance.getCpu();
        actualHostStates[hostId * HostState.STATE_NUM + 1] += instance.getRam();
        actualHostStates[hostId * HostState.STATE_NUM + 2] += instance.getStorage();
        actualHostStates[hostId * HostState.STATE_NUM + 3] += instance.getBw();

//        simpleState.updateSimpleStateReleased(hostId, beforeHostState, instance);
        datacenterPowerOnRecord.hostReleaseInstance(hostId, datacenter.getSimulation().clock());
        return this;
    }

    @Override
    public Object getStateByType(String type) {
        return switch (type) {
            case "detailed" ->
                    new DetailedDcStateSimple(getCenterHostStates(), hostCapacityManager, simpleState.getCpuAvailableSum(), simpleState.getRamAvailableSum(), simpleState.getStorageAvailableSum(), simpleState.getBwAvailableSum());
            case "easySimple" -> simpleState.generate();
            case "null" -> null;
            default -> throw new IllegalArgumentException("Unrecognized state type: " + type);
        };
    }

    @Override
    public boolean getPredictable() {
        return predictable;
    }

    @Override
    public StatesManager setPredictable(boolean predictable) {
        this.predictable = predictable;
        return this;
    }

    @Override
    public HostState getActualHostState(int hostId) {
        return new HostState(
                actualHostStates[hostId * HostState.STATE_NUM],
                actualHostStates[hostId * HostState.STATE_NUM + 1],
                actualHostStates[hostId * HostState.STATE_NUM + 2],
                actualHostStates[hostId * HostState.STATE_NUM + 3]);
    }

    @Override
    public HostState getCenterHostState(int hostId) {
        int[] hostStates = getCenterHostStates();
        return new HostState(
                hostStates[hostId * HostState.STATE_NUM],
                hostStates[hostId * HostState.STATE_NUM + 1],
                hostStates[hostId * HostState.STATE_NUM + 2],
                hostStates[hostId * HostState.STATE_NUM + 3]);
    }

    @Override
    public StatesManager revertHostState(IntraSchedulerResult intraSchedulerResult) {
        int smallSynGapCount = synGapManager.getPartitionSynCount();
        IntraScheduler intraScheduler = intraSchedulerResult.getIntraScheduler();
        Set<Integer> clearPartitions = new HashSet<>();
        while (clearPartitions.size() != partitionNum && smallSynGapCount >= 0) {
            double time = synGapManager.getSynTime(smallSynGapCount);
            if (time < intraSchedulerResult.getScheduleTime()) {
                break;
            }
            clearPartitions.add((smallSynGapCount + intraScheduler.getFirstPartitionId()) % partitionNum);
            smallSynGapCount--;
        }
//        LOGGER.info("{}: revertHostState: clearPartitions: {}", datacenter.getSimulation().clock(), clearPartitions);

        for (Instance instance : intraSchedulerResult.getScheduledInstances()) {
            int hostId = instance.getExpectedScheduleHostId();
            int partitionId = partitionRangesManager.getPartitionId(hostId);
            if (clearPartitions.contains(partitionId)) {
                int[] hostState;
                if (selfHostStateMap.get(intraScheduler).get(partitionId).containsKey(hostId)) {
                    hostState = selfHostStateMap.get(intraScheduler).get(partitionId).get(hostId);
                }else{
                    hostState = getLatestSynHostState(hostId);
                }
                hostState[0] -= instance.getCpu();
                hostState[1] -= instance.getRam();
                hostState[2] -= instance.getStorage();
                hostState[3] -= instance.getBw();
                selfHostStateMap.get(intraScheduler).get(partitionId).put(hostId, hostState);
            }
        }
        return this;
    }

    /**
     * Get the latest synchronized host state of the host with hostId.
     * @param hostId the id of the host.
     * @return the host state.
     */
    private int[] getLatestSynHostState(int hostId) {
        double time = synGapManager.getSynTime(synGapManager.getPartitionSynCount());
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        Map<Integer, int[]> partitionSynState = synStateMap.get(partitionId).get(time);
        if (partitionSynState.containsKey(hostId)) {
            return partitionSynState.get(hostId);
        } else {
            int[] hostState = new int[HostState.STATE_NUM];
            System.arraycopy(getCenterHostStates(), hostId * HostState.STATE_NUM, hostState, 0, HostState.STATE_NUM);
            return hostState;
        }
    }

    @Override
    public StatesManager revertSelfHostState(List<Instance> instances, IntraScheduler intraScheduler) {
        Map<Integer, Map<Integer, int[]>> selfHostState = selfHostStateMap.get(intraScheduler);
        for (Instance instance : instances) {
            if (instance.getRetryHostIds() == null || instance.getRetryHostIds().isEmpty()) {
                LOGGER.error("{}: instance {} has no retry host id in revertSelftHostState function", getDatacenter().getSimulation().clockStr(), instance.getId());
                System.exit(-1);
            }
            int hostId = instance.getRetryHostIds().get(instance.getRetryHostIds().size() - 1);
            int[] hostState = selfHostState.get(partitionRangesManager.getPartitionId(hostId)).get(hostId);
            hostState[0] += instance.getCpu();
            hostState[1] += instance.getRam();
            hostState[2] += instance.getStorage();
            hostState[3] += instance.getBw();
        }
        return this;
    }

    @Override
    public double getNextHeartbeatDelay(int hostId, double currentTime) {
        if (!isNeedHeartbeat()) {
            return 0;
        }
        int heartBeatInitSendTime = heartbeatSendTime[hostId];
        int nextHeartBeatTime = (int) ((currentTime - heartBeatInitSendTime) / heartbeatInterval) * heartbeatInterval + heartbeatInterval + heartBeatInitSendTime;
        return nextHeartBeatTime - currentTime;
    }

    @Override
    public boolean isSynCostTime() {
        return synGapManager.isSynCostTime();
    }

    @Override
    public double getNextPartitionSynDelay() {
        return synGapManager.getNextSynDelay(datacenter.getSimulation().clock());
    }

    @Override
    public int getPartitionSynCount() {
        return synGapManager.getPartitionSynCount();
    }

    @Override
    public boolean isInLatestPartitionSynGap(double time) {
        return time >= synGapManager.getSynTime(synGapManager.getPartitionSynCount());
    }

    @Override
    public boolean allocate(Instance instance) {
        return allocate(instance.getExpectedScheduleHostId(), instance);
    }

    @Override
    public int[] getHostCapacity(int hostId) {
        return hostCapacityManager.getHostCapacity(hostId);
    }

    @Override
    public StatesManager adjustScheduleView() {
        if(Objects.equals(datacenter.getArchitecture(), "two-level")) {
            return adjustScheduleViewOfDynamicAvg();
        } else {
            return adjustScheduleViewToAll();
        }
    }

    /**
     * Adjust the scheduling view of each intra-scheduler and divide it evenly according to the remaining resources of the hosts in the current data center.
     * @return the StatesManager itself.
     */
    private StatesManager adjustScheduleViewOfDynamicAvg(){
        long cpuAvailableSum = simpleState.getCpuAvailableSum();
        int innerSchedulerNum = getDatacenter().getIntraSchedulers().size();
        long averageCpuAvailable = cpuAvailableSum / innerSchedulerNum;

        int startIndex = 0;
        int innerSchedulerId = 0;
        long tmpCpuAvailableSum = 0;
        for(int hostId = 0; hostId<hostNum && innerSchedulerId < innerSchedulerNum-1;hostId++) {
            tmpCpuAvailableSum += getActualHostState(hostId).getCpu();
            if(tmpCpuAvailableSum>=averageCpuAvailable){
                IntraScheduler intraScheduler = getDatacenter().getIntraSchedulers().get(innerSchedulerId);
                intraSchedulerView.putIfAbsent(intraScheduler, List.of(startIndex, hostId));
                startIndex = hostId+1;
                innerSchedulerId++;
                tmpCpuAvailableSum = 0;
            }
        }
        IntraScheduler intraScheduler = getDatacenter().getIntraSchedulers().get(innerSchedulerId);
        intraSchedulerView.putIfAbsent(intraScheduler, List.of(startIndex, hostNum - 1));

        return this;
    }

    /**
     * Set the scheduling view of each scheduler to be all hosts.
     * @return the StatesManager itself.
     */
    private StatesManager adjustScheduleViewToAll(){
        for (IntraScheduler intraScheduler : getDatacenter().getIntraSchedulers()) {
            intraSchedulerView.putIfAbsent(intraScheduler, List.of(0, hostNum - 1));
        }

        return this;
    }

    @Override
    public List<Integer> getIntraSchedulerView(IntraScheduler intraScheduler) {
        return intraSchedulerView.get(intraScheduler);
    }

    /**
     * Before the host state changes,
     * save the previous host state to ensure that the synchronized host state will not change in real time.
     *
     * @param hostId       The host id to be changed
     * @param synHostState The host state before the change
     */
    private void updateSynStateMap(int hostId, int[] synHostState) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        TreeMap<Double, Map<Integer, int[]>> partitionSynStateMap = synStateMap.get(partitionId);
        double largestSynTime = partitionSynStateMap.lastKey();
        if (!partitionSynStateMap.get(largestSynTime).containsKey(hostId)) {
            partitionSynStateMap.get(largestSynTime).put(hostId, synHostState);
        }
    }

    /**
     * Initialize the synStateMap.
     */
    private void initSynStateMap() {
        synStateMap = new HashMap<>();
        for (int partitionId : partitionRangesManager.getPartitionIds()) {
            TreeMap<Double, Map<Integer, int[]>> partitionSynStateMap = new TreeMap<>();
            partitionSynStateMap.put(0.0, new HashMap<>());
            synStateMap.put(partitionId, partitionSynStateMap);
        }
    }

    private int[] getCenterHostStates() {
        if (isNeedHeartbeat()) {
            return centerHostStates;
        } else {
            return actualHostStates;
        }
    }

    @Override
    public boolean isNeedHeartbeat() {
        return heartbeatInterval > 0;
    }

    @Override
    public long getTotalCPU() {
        return hostCapacityManager.getCpuCapacitySum();
    }

    @Override
    public long getTotalRAM() {
        return hostCapacityManager.getRamCapacitySum();
    }

    @Override
    public long getTotalStorage() {
        return hostCapacityManager.getStorageCapacitySum();
    }

    @Override
    public long getTotalBw() {
        return hostCapacityManager.getBwCapacitySum();
    }
}
