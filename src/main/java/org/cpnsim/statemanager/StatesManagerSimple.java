package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterPowerOnRecord;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;

public class StatesManagerSimple implements StatesManager {
    private int[] hostStates;
    @Getter
    private int totalCpuInUse;
    @Getter
    private int totalRamInUse;
    @Getter
    private int totalStorageInUse;
    @Getter
    private int totalBwInUse;
    private double synGap;
    private boolean predictable;
    private Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap;
    private Map<InnerScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap;
    private int partitionNum;
    @Getter
    private int hostNum;
    @Getter
    private DatacenterPowerOnRecord datacenterPowerOnRecord;
    @Getter
    private PartitionRangesManager partitionRangesManager;
    @Getter
    private double smallSynGap;
    @Getter
    @Setter
    private PredictionManager predictionManager;
    @Getter
    @Setter
    private int predictRecordNum = 0;
    @Getter
    @Setter
    private Datacenter datacenter;
    @Getter
    @Setter
    private SimpleState simpleState;
    public StatesManagerSimple(int hostNum, PartitionRangesManager partitionRangesManager, double synGap) {
        this.hostNum = hostNum;
        this.hostStates = new int[hostNum * HostState.STATE_NUM];
        this.partitionRangesManager = partitionRangesManager;
        this.synGap = synGap;
        this.smallSynGap = synGap / partitionRangesManager.getPartitionNum();
        this.simpleState = new SimpleStateSimple();
        this.partitionNum = partitionRangesManager.getPartitionNum();
        this.selfHostStateMap = new HashMap<>();
        this.datacenterPowerOnRecord = new DatacenterPowerOnRecord();
        this.totalCpuInUse = 0;
        this.totalRamInUse = 0;
        this.totalStorageInUse = 0;
        this.totalBwInUse = 0;
        initSynStateMap();
    }

    @Override
    public StatesManager initHostStates(int cpu, int ram, int storage, int bw, int startId, int length) {
        int endId = startId + length - 1;
        for (int i = startId; i <= endId; i++) {
            initSingleHostState(i, cpu, ram, storage, bw);
        }
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
        simpleState.addCpuRamRecord(state[0], state[1]);
        simpleState.updateStorageSum(state[2]);
        simpleState.updateBwSum(state[3]);
    }

    private void initSingleHostState(int hostId, int cpu, int ram, int storage, int bw) {
        initSingleHostState(hostId, new int[] { cpu, ram, storage, bw });
    }

    @Override
    public SynState getSynState(InnerScheduler scheduler) {
        Map<Integer, Map<Integer, int[]>> selfHostState;
        int[] partitionIds = partitionRangesManager.getPartitionIds();

        if (!selfHostStateMap.containsKey(scheduler) || synGap == 0) {
            selfHostStateMap.put(scheduler, new HashMap<>());
            for (int partitionId : partitionIds) {
                selfHostStateMap.get(scheduler).put(partitionId, new HashMap<>());
            }
        }
        selfHostState = selfHostStateMap.get(scheduler);
        return new SynStateSimple(synStateMap, hostStates, partitionRangesManager, selfHostState, scheduler, predictionManager, getDatacenter().getSimulation().clock(), smallSynGap, synGap, predictRecordNum, predictable);
    }

    @Override
    public StatesManager synAllState() {
        if (synGap == 0) {
            return this;
        }
        double nowTime = datacenter.getSimulation().clock();
        for (Map<Double, Map<Integer, int[]>> partitionSynStateMap : synStateMap.values()) {
            if (!predictable && nowTime - synGap >= 0) {
                partitionSynStateMap.remove(nowTime - synGap);
            } else if (predictable && nowTime - synGap * predictRecordNum >= 0) {
                partitionSynStateMap.remove(nowTime - synGap * predictRecordNum);
            }
            partitionSynStateMap.put(nowTime, new HashMap<>());
        }
        for (InnerScheduler scheduler : selfHostStateMap.keySet()) {
            int clearPartitionId = ((int) (nowTime / smallSynGap) + scheduler.getFirstPartitionId()) % partitionNum;
            selfHostStateMap.get(scheduler).get(clearPartitionId).clear();
        }
        return this;
    }

    @Override
    public boolean allocate(int hostId, Instance instance) {
        int[] synHostState = new int[HostState.STATE_NUM];
        System.arraycopy(hostStates, hostId * HostState.STATE_NUM, synHostState, 0, HostState.STATE_NUM);
        if (synHostState[0] < instance.getCpu() || synHostState[1] < instance.getRam() || synHostState[2] < instance.getStorage() || synHostState[3] < instance.getBw()) {
            return false;//一般不会发生
        }

        updateSynStateMap(hostId, synHostState);

        hostStates[hostId * HostState.STATE_NUM] -= instance.getCpu();
        hostStates[hostId * HostState.STATE_NUM + 1] -= instance.getRam();
        hostStates[hostId * HostState.STATE_NUM + 2] -= instance.getStorage();
        hostStates[hostId * HostState.STATE_NUM + 3] -= instance.getBw();

        // update total ... in use
        totalCpuInUse += instance.getCpu();
        totalRamInUse += instance.getRam();
        totalStorageInUse += instance.getStorage();
        totalBwInUse += instance.getBw();

        updateSimpleState(hostId, synHostState);
        datacenterPowerOnRecord.hostAllocateInstance(hostId, datacenter.getSimulation().clock());
        return true;
    }

    @Override
    public StatesManager release(int hostId, Instance instance) {
        int[] synHostState = new int[HostState.STATE_NUM];
        System.arraycopy(hostStates, hostId * HostState.STATE_NUM, synHostState, 0, HostState.STATE_NUM);

        updateSynStateMap(hostId, synHostState);

        hostStates[hostId * HostState.STATE_NUM] += instance.getCpu();
        hostStates[hostId * HostState.STATE_NUM + 1] += instance.getRam();
        hostStates[hostId * HostState.STATE_NUM + 2] += instance.getStorage();
        hostStates[hostId * HostState.STATE_NUM + 3] += instance.getBw();

        // update total ... in use
        totalCpuInUse -= instance.getCpu();
        totalRamInUse -= instance.getRam();
        totalStorageInUse -= instance.getStorage();
        totalBwInUse -= instance.getBw();

        updateSimpleState(hostId, synHostState);
        datacenterPowerOnRecord.hostReleaseInstance(hostId, datacenter.getSimulation().clock());
        return this;
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
    public StatesManager revertHostState(Map<Integer, List<Instance>> scheduleResult, InnerScheduler innerScheduler) {
        int clearPartitionId = ((int) (datacenter.getSimulation().clock() / smallSynGap) + innerScheduler.getFirstPartitionId()) % partitionNum;
        for (int hostId : scheduleResult.keySet()) {
            if (partitionRangesManager.getPartitionId(hostId) == clearPartitionId) {
                int[] hostState = new int[HostState.STATE_NUM];
                System.arraycopy(hostStates, hostId * HostState.STATE_NUM, hostState, 0, HostState.STATE_NUM);
                for (Instance instance : scheduleResult.get(hostId)) {
                    hostState[0] -= instance.getCpu();
                    hostState[1] -= instance.getRam();
                    hostState[2] -= instance.getStorage();
                    hostState[3] -= instance.getBw();
                }
                selfHostStateMap.get(innerScheduler).get(clearPartitionId).put(hostId, hostState);
            }
        }
        return this;
    }

    private void updateSynStateMap(int hostId, int[] synHostState) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        TreeMap<Double, Map<Integer, int[]>> partitionSynStateMap = synStateMap.get(partitionId);
        double largestSynTime = partitionSynStateMap.lastKey();
        if (!partitionSynStateMap.get(largestSynTime).containsKey(hostId)) {
            partitionSynStateMap.get(largestSynTime).put(hostId, synHostState);
        }
    }

    private void updateSimpleState(int hostId, int[] synHostState) {
        simpleState.updateCpuRamMap(synHostState[0], synHostState[1],
                hostStates[hostId * HostState.STATE_NUM], hostStates[hostId * HostState.STATE_NUM + 1]);
        simpleState.updateStorageSum(hostStates[hostId * HostState.STATE_NUM + 2] - synHostState[2]);
        simpleState.updateBwSum(hostStates[hostId * HostState.STATE_NUM + 3] - synHostState[3]);
    }

    private void initSynStateMap() {
        synStateMap = new HashMap<>();
        for (int partitionId : partitionRangesManager.getPartitionIds()) {
            TreeMap<Double, Map<Integer, int[]>> partitionSynStateMap = new TreeMap<>();
            partitionSynStateMap.put(0.0, new HashMap<>());
            synStateMap.put(partitionId, partitionSynStateMap);
        }
    }
}
