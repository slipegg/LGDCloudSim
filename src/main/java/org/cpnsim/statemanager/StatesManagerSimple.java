package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterPowerOnRecord;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.innerscheduler.InnerSchedulerResult;
import org.cpnsim.request.Instance;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;

/**
 * A class to manage the states of datacenter.
 * This class implements the interface {@link StatesManager}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class StatesManagerSimple implements StatesManager {
    /**
     * The status of all hosts at the current time
     **/
    private int[] hostStates;
    @Getter
    private int totalCpuInUse;
    @Getter
    private int totalRamInUse;
    @Getter
    private int totalStorageInUse;
    @Getter
    private int totalBwInUse;

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
    private Map<InnerScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap;

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
    private SimpleState simpleState;

    @Getter
    private int maxCpuCapacity;

    @Getter
    private int maxRamCapacity;

    private SynGapManager synGapManager;

    @Getter
    private HostCapacityManager hostCapacityManager;

    private Map<InnerScheduler, List<Integer>> innerSchedulerView;

    public StatesManagerSimple(int hostNum, PartitionRangesManager partitionRangesManager, double synGap, int maxCpuCapacity, int maxRamCapacity) {
        this.hostNum = hostNum;
        this.hostStates = new int[hostNum * HostState.STATE_NUM];
        this.partitionRangesManager = partitionRangesManager;
        this.synGapManager = new SynGapManager(synGap, partitionRangesManager.getPartitionNum());
        this.maxCpuCapacity = maxCpuCapacity;
        this.maxRamCapacity = maxRamCapacity;
        this.simpleState = new SimpleStateEasy(this);
        this.partitionNum = partitionRangesManager.getPartitionNum();
        this.selfHostStateMap = new HashMap<>();
        this.datacenterPowerOnRecord = new DatacenterPowerOnRecord();
        this.hostCapacityManager = new HostCapacityManager();
        this.innerSchedulerView = new HashMap<>();
        this.totalCpuInUse = 0;
        this.totalRamInUse = 0;
        this.totalStorageInUse = 0;
        this.totalBwInUse = 0;
        initSynStateMap();
    }

    public StatesManagerSimple(int hostNum, PartitionRangesManager partitionRangesManager, double synGap) {
        this(hostNum, partitionRangesManager, synGap, 128, 256);
    }

    //TODO: initHostStates不需要是有序的，但是这里却要求了hostCapacityManager初始化是有序的，后面需要修改
    //但是因为现在只在initDatacenter中使用了这个，所以还没什么问题
    @Override
    public StatesManager initHostStates(int cpu, int ram, int storage, int bw, int startId, int length) {
        int endId = startId + length - 1;
        for (int i = startId; i <= endId; i++) {
            initSingleHostState(i, cpu, ram, storage, bw);
        }
        hostCapacityManager.orderlyAddSameCapacityHost(length,new int[]{cpu,ram,storage,bw});
        return this;
    }

    @Override
    public StatesManager initHostStates(HostStateGenerator hostStateGenerator) {
        for (int i = 0; i < hostNum; i++) {
            int[] state = hostStateGenerator.generateHostState();
            initSingleHostState(i, state);
        }
        return this;
    }

    private void initSingleHostState(int hostId, int[] state) {
        if (state.length != HostState.STATE_NUM) {
            throw new IllegalArgumentException("Host state must be array of size " + HostState.STATE_NUM);
        }
        System.arraycopy(state, 0, hostStates, hostId * HostState.STATE_NUM, HostState.STATE_NUM);
        simpleState.initHostSimpleState(hostId, state);
    }

    private void initSingleHostState(int hostId, int cpu, int ram, int storage, int bw) {
        initSingleHostState(hostId, new int[] { cpu, ram, storage, bw });
    }

    @Override
    public SynState getSynState(InnerScheduler scheduler) {
        Map<Integer, Map<Integer, int[]>> selfHostState;
        int[] partitionIds = partitionRangesManager.getPartitionIds();

        if (!selfHostStateMap.containsKey(scheduler) || !synGapManager.isSynCostTime()) {
            selfHostStateMap.put(scheduler, new HashMap<>());
            for (int partitionId : partitionIds) {
                selfHostStateMap.get(scheduler).put(partitionId, new HashMap<>());
            }
        }
        selfHostState = selfHostStateMap.get(scheduler);
        return new SynStateSimple(synStateMap, hostStates, partitionRangesManager, selfHostState, scheduler, predictionManager, synGapManager, predictRecordNum, predictable);
    }

    /**
     * Synchronize one area for each {@link InnerScheduler}.
     */
    @Override
    public StatesManager synAllState() {
        if (!isSynCostTime()) {
            return this;
        }
        synGapManager.synGapCountAddOne();
        int latestSmallSynGapCount = synGapManager.getSmallSynGapCount();
        for (Map<Double, Map<Integer, int[]>> partitionSynStateMap : synStateMap.values()) {
            if (!predictable && latestSmallSynGapCount >= partitionNum) {
                partitionSynStateMap.remove(synGapManager.getSynTime(latestSmallSynGapCount - partitionNum));
            } else if (predictable && latestSmallSynGapCount >= partitionNum * getPredictRecordNum()) {
                partitionSynStateMap.remove(synGapManager.getSynTime(latestSmallSynGapCount - partitionNum * getPredictRecordNum()));
            }
            partitionSynStateMap.put(synGapManager.getSynTime(latestSmallSynGapCount), new HashMap<>());
        }
        for (InnerScheduler scheduler : selfHostStateMap.keySet()) {
            int clearPartitionId = (latestSmallSynGapCount + scheduler.getFirstPartitionId()) % partitionNum;
            selfHostStateMap.get(scheduler).get(clearPartitionId).clear();
        }
        return this;
    }

    @Override
    public boolean allocate(int hostId, Instance instance) {
        int[] beforeHostState = new int[HostState.STATE_NUM];
        System.arraycopy(hostStates, hostId * HostState.STATE_NUM, beforeHostState, 0, HostState.STATE_NUM);
        if (beforeHostState[0] < instance.getCpu() || beforeHostState[1] < instance.getRam() || beforeHostState[2] < instance.getStorage() || beforeHostState[3] < instance.getBw()) {
            return false;//一般不会发生
        }

        updateSynStateMap(hostId, beforeHostState);

        hostStates[hostId * HostState.STATE_NUM] -= instance.getCpu();
        hostStates[hostId * HostState.STATE_NUM + 1] -= instance.getRam();
        hostStates[hostId * HostState.STATE_NUM + 2] -= instance.getStorage();
        hostStates[hostId * HostState.STATE_NUM + 3] -= instance.getBw();

        // update total ... in use
        totalCpuInUse += instance.getCpu();
        totalRamInUse += instance.getRam();
        totalStorageInUse += instance.getStorage();
        totalBwInUse += instance.getBw();

        simpleState.updateSimpleStateAllocated(hostId, beforeHostState, instance);
        datacenterPowerOnRecord.hostAllocateInstance(hostId, datacenter.getSimulation().clock());
        return true;
    }

    @Override
    public StatesManager release(int hostId, Instance instance) {
        int[] beforeHostState = new int[HostState.STATE_NUM];
        System.arraycopy(hostStates, hostId * HostState.STATE_NUM, beforeHostState, 0, HostState.STATE_NUM);

        updateSynStateMap(hostId, beforeHostState);

        hostStates[hostId * HostState.STATE_NUM] += instance.getCpu();
        hostStates[hostId * HostState.STATE_NUM + 1] += instance.getRam();
        hostStates[hostId * HostState.STATE_NUM + 2] += instance.getStorage();
        hostStates[hostId * HostState.STATE_NUM + 3] += instance.getBw();

        // update total ... in use
        totalCpuInUse -= instance.getCpu();
        totalRamInUse -= instance.getRam();
        totalStorageInUse -= instance.getStorage();
        totalBwInUse -= instance.getBw();

        simpleState.updateSimpleStateReleased(hostId, beforeHostState, instance);
        datacenterPowerOnRecord.hostReleaseInstance(hostId, datacenter.getSimulation().clock());
        return this;
    }

    @Override
    public Object getStateByType(String type) {
        return switch (type) {
            case "detailed" -> new DetailedDcStateSimple(hostStates,hostCapacityManager, simpleState.getCpuAvailableSum(), simpleState.getRamAvailableSum(), simpleState.getStorageAvailableSum(), simpleState.getBwAvailableSum());
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
    public HostState getNowHostState(int hostId) {
        HostState hostState = new HostState(
                hostStates[hostId * HostState.STATE_NUM],
                hostStates[hostId * HostState.STATE_NUM + 1],
                hostStates[hostId * HostState.STATE_NUM + 2],
                hostStates[hostId * HostState.STATE_NUM + 3]);
        return hostState;
    }

    @Override
    public StatesManager revertHostState(InnerSchedulerResult innerSchedulerResult) {
        int smallSynGapCount = synGapManager.getSmallSynGapCount();
        InnerScheduler innerScheduler = innerSchedulerResult.getInnerScheduler();
        Set<Integer> clearPartitions = new HashSet<>();
        while (clearPartitions.size() != partitionNum && smallSynGapCount >= 0) {
            double time = synGapManager.getSynTime(smallSynGapCount);
            if (time < innerSchedulerResult.getScheduleTime()) {
                break;
            }
            clearPartitions.add((smallSynGapCount + innerScheduler.getFirstPartitionId()) % partitionNum);
            smallSynGapCount--;
        }
//        LOGGER.info("{}: revertHostState: clearPartitions: {}", datacenter.getSimulation().clock(), clearPartitions);

        for (Instance instance : innerSchedulerResult.getScheduledInstances()) {
            int hostId = instance.getExpectedScheduleHostId();
            int partitionId = partitionRangesManager.getPartitionId(hostId);
            if (clearPartitions.contains(partitionId)) {
                int[] hostState;
                if (selfHostStateMap.get(innerScheduler).get(partitionId).containsKey(hostId)){
                    hostState = selfHostStateMap.get(innerScheduler).get(partitionId).get(hostId);
                }else{
                    hostState = getLatestSynHostState(hostId);
                }
                hostState[0] -= instance.getCpu();
                hostState[1] -= instance.getRam();
                hostState[2] -= instance.getStorage();
                hostState[3] -= instance.getBw();
                selfHostStateMap.get(innerScheduler).get(partitionId).put(hostId, hostState);
            }
        }
        return this;
    }

    private int[] getLatestSynHostState(int hostId) {
        double time = synGapManager.getSynTime(synGapManager.getSmallSynGapCount());
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        Map<Integer, int[]> partitionSynState = synStateMap.get(partitionId).get(time);
        if (partitionSynState.containsKey(hostId)) {
            return partitionSynState.get(hostId);
        } else {
            int[] hostState = new int[HostState.STATE_NUM];
            System.arraycopy(hostStates, hostId * HostState.STATE_NUM, hostState, 0, HostState.STATE_NUM);
            return hostState;
        }
    }

    @Override
    public StatesManager revertSelftHostState(List<Instance> instances, InnerScheduler innerScheduler) {
        Map<Integer, Map<Integer, int[]>> selfHostState = selfHostStateMap.get(innerScheduler);
//        LOGGER.error("{}: revert self host state:{}", getDatacenter().getSimulation().clockStr(), innerScheduler.getName());
//        for(Map.Entry<Integer, Map<Integer, int[]>> entry:selfHostState.entrySet()){
//            LOGGER.error("partitionId:{},hostState.size():{}",entry.getKey(),entry.getValue().size());
//        }
        for (Instance instance : instances) {
            if (instance.getRetryHostIds() == null || instance.getRetryHostIds().size() == 0) {
                LOGGER.error("{}: instance {} has no retry host id in revertSelftHostState function", getDatacenter().getSimulation().clockStr(), instance.getId());
                System.exit(-1);
            }
//            LOGGER.error("instance{} retryHostIds:{}", instance.getId(),instance.getRetryHostIds());
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
    public boolean isSynCostTime() {
        return synGapManager.isSynCostTime();
    }

    @Override
    public double getNextSynDelay() {
        return synGapManager.getNextSynDelay(datacenter.getSimulation().clock());
    }

    @Override
    public int getSmallSynGapCount() {
        return synGapManager.getSmallSynGapCount();
    }

    @Override
    public boolean isInLatestSmallSynGap(double time) {
        return time >= synGapManager.getSynTime(synGapManager.getSmallSynGapCount());
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
        }else{
            return adjustScheduleViewToAll();
        }
    }

    private StatesManager adjustScheduleViewOfDynamicAvg(){
        long cpuAvailableSum = simpleState.getCpuAvailableSum();
        int innerSchedulerNum = getDatacenter().getInnerSchedulers().size();
        long averageCpuAvailable = cpuAvailableSum / innerSchedulerNum;

        int startIndex = 0;
        int innerSchedulerId = 0;
        long tmpCpuAvailableSum = 0;
        for(int hostId = 0; hostId<hostNum && innerSchedulerId < innerSchedulerNum-1;hostId++){
            tmpCpuAvailableSum+=getNowHostState(hostId).getCpu();
            if(tmpCpuAvailableSum>=averageCpuAvailable){
                InnerScheduler innerScheduler = getDatacenter().getInnerSchedulers().get(innerSchedulerId);
                innerSchedulerView.putIfAbsent(innerScheduler, List.of(startIndex, hostId));
                startIndex = hostId+1;
                innerSchedulerId++;
                tmpCpuAvailableSum = 0;
            }
        }
        InnerScheduler innerScheduler = getDatacenter().getInnerSchedulers().get(innerSchedulerId);
        innerSchedulerView.putIfAbsent(innerScheduler, List.of(startIndex, hostNum-1));

        return this;
    }

    private StatesManager adjustScheduleViewToAll(){
        for(InnerScheduler innerScheduler : getDatacenter().getInnerSchedulers()){
            innerSchedulerView.putIfAbsent(innerScheduler, List.of(0, hostNum-1));
        }

        return this;
    }

    @Override
    public List<Integer> getInnerSchedulerView(InnerScheduler innerScheduler){
        return innerSchedulerView.get(innerScheduler);
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
     * Update the simple state of the host after the host state changes.
     *
     * @param hostId       The host id to be changed
     * @param synHostState The host state before the change
     */
//    private void updateSimpleState(int hostId, int[] synHostState) {
//        simpleState.updateCpuRamMap(synHostState[0], synHostState[1],
//                hostStates[hostId * HostState.STATE_NUM], hostStates[hostId * HostState.STATE_NUM + 1]);
//        simpleState.updateStorageSum(hostStates[hostId * HostState.STATE_NUM + 2] - synHostState[2]);
//        simpleState.updateBwSum(hostStates[hostId * HostState.STATE_NUM + 3] - synHostState[3]);
//    }

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
}
