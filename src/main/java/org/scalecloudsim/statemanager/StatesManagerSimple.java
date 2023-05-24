package org.scalecloudsim.statemanager;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.datacenter.DatacenterPowerOnRecord;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.request.Instance;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;

public class StatesManagerSimple implements StatesManager {
    int[] hostStates;
    @Getter
    int hostNum;
    //partitionId,synTime,hostId,hostState
    Map<Integer, Map<Double, Map<Integer, int[]>>> synStateMap;
    Map<InnerScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap;
    @Getter
    @Setter
    Datacenter datacenter;

    @Getter
    DatacenterPowerOnRecord datacenterPowerOnRecord;

    @Getter
    @Setter
    SimpleState simpleState;

    @Getter
    PartitionRangesManager partitionRangesManager;

    double synGap;

    @Getter
    double smallSynGap;

    int partitionNum;


    boolean predictable;

    @Getter
    @Setter
    PredictionManager predictionManager;
//    Map<Integer,int[]> oldState;

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
        initSynStateMap();

//        oldState=new HashMap<>();
//        for(int i=0;i<partitionRangesManager.ranges.size();i++){
//            oldState.put(i,new int[hostNum * HostState.STATE_NUM]);
//        }
    }

    private void initSynStateMap() {
        synStateMap = new HashMap<>();
        for (int partitionId : partitionRangesManager.getPartitionIds()) {
            Map<Double, Map<Integer, int[]>> partitionSynStateMap = new TreeMap<>();
            partitionSynStateMap.put(0.0, new HashMap<>());
            synStateMap.put(partitionId, partitionSynStateMap);
        }
    }

    @Override
    public StatesManager initHostStates(int cpu, int ram, int storage, int bw, int startId, int length) {
        int endId = startId + length - 1;
        for (int i = startId; i <= endId; i++) {
            hostStates[i * HostState.STATE_NUM] = cpu;
            hostStates[i * HostState.STATE_NUM + 1] = ram;
            hostStates[i * HostState.STATE_NUM + 2] = storage;
            hostStates[i * HostState.STATE_NUM + 3] = bw;
            simpleState.addCpuRamRecord(cpu, ram);
            simpleState.updateStorageSum(storage);
            simpleState.updateBwSum(bw);
        }
        return this;
    }

    @Override
    public SynState getSynState(InnerScheduler scheduler) {
        Map<Integer, Map<Integer, int[]>> synState = new HashMap<>();
        Map<Integer, Map<Integer, int[]>> selfHostState;
        int[] partitionIds = partitionRangesManager.getPartitionIds();

        if (!selfHostStateMap.containsKey(scheduler) || synGap == 0) {
            selfHostStateMap.put(scheduler, new HashMap<>());
            for (int partitionId : partitionIds) {
                selfHostStateMap.get(scheduler).put(partitionId, new HashMap<>());
            }
        }
        selfHostState = selfHostStateMap.get(scheduler);
        if (synGap == 0) {
            for (int partitionId : partitionIds) {
                synState.put(partitionId, new HashMap<>());
            }
            return new SynStateSimple(synState, hostStates, partitionRangesManager, selfHostState);
        } else {
            int smallSynNum = (int) (datacenter.getSimulation().clock() / smallSynGap);
            int synPartitionId = (scheduler.getFirstPartitionId() + smallSynNum) % partitionNum;
            for (int i = 0; i < partitionNum; i++) {
                int partitionId = (synPartitionId + i) % partitionNum;
                double synTime = max(0.0, smallSynGap * (smallSynNum - i));
                synState.put(partitionId, synStateMap.get(partitionId).get(synTime));
            }
            return new SynStateSimple(synState, hostStates, partitionRangesManager, selfHostState);
        }
    }


    @Override
    public StatesManager synAllState() {
        if (synGap == 0) {
            return this;
        }
        double nowTime = datacenter.getSimulation().clock();
        for (Map<Double, Map<Integer, int[]>> partitionSynStateMap : synStateMap.values()) {
            if (nowTime - synGap >= 0) {
                partitionSynStateMap.remove(nowTime - synGap);
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

        updateSimpleState(hostId, synHostState, instance);
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

        updateSimpleState(hostId, synHostState, instance);
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
    public StatesManager initHostStates(HostStateGenerator hostStateGenerator) {
        for (int i = 0; i < hostNum; i++) {
            int[] state = hostStateGenerator.generateHostState();
            System.arraycopy(state, 0, hostStates, i * HostState.STATE_NUM, HostState.STATE_NUM);
            simpleState.addCpuRamRecord(state[0], state[1]);
            simpleState.updateStorageSum(state[2]);
            simpleState.updateBwSum(state[3]);
        }
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
        Map<Double, Map<Integer, int[]>> partitionSynStateMap = synStateMap.get(partitionId);
        for (Double synTime : partitionSynStateMap.keySet()) {
            if (!partitionSynStateMap.get(synTime).containsKey(hostId)) {//第一次变化的时候将原本的状态存入进去，如果原本就存了就不需要变
                partitionSynStateMap.get(synTime).put(hostId, synHostState);
            }
        }
    }

    private void updateSimpleState(int hostId, int[] synHostState, Instance instance) {
        simpleState.updateCpuRamMap(synHostState[0], synHostState[1],
                hostStates[hostId * HostState.STATE_NUM], hostStates[hostId * HostState.STATE_NUM + 1]);
        simpleState.updateStorageSum(-1 * instance.getStorage());
        simpleState.updateBwSum(-1 * instance.getBw());
    }

}
