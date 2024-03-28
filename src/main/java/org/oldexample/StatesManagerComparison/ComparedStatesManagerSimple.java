package org.oldexample.StatesManagerComparison;

import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.intrascheduler.IntraSchedulerSimple;
import org.lgdcloudsim.record.MemoryRecord;
import org.lgdcloudsim.statemanager.*;

import java.lang.reflect.Field;
import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;

public class ComparedStatesManagerSimple {
    double allTime = 5000;
    int schedulerNum = 10;
    int hostNum = 10_000_000;
    int partitionNum = 100;
    int instanceNumPerMillSec = 100;
    int synGap = 500;
    int smallSynGap = synGap / partitionNum;
    int hostCpu = 100;
    int hostRam = 100;
    int hostStorage = 100;
    int hostBw = 100;
    StatesManagerSimple statesManager;
    Random random = new Random(1);
    //    int smallSynCountNow = 0;
    SynGapManager synGapManager;
    List<IntraScheduler> intraSchedulers;

    public static void main(String[] args) {
        new ComparedStatesManagerSimple();
    }

    public ComparedStatesManagerSimple() {
        double startTime = System.currentTimeMillis();

        PartitionRangesManager partitionRangesManager = new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0, hostNum - 1, 100);
        statesManager = new StatesManagerSimple(hostNum, partitionRangesManager, synGap);
        statesManager.initHostStates(hostCpu, hostRam, hostStorage, hostBw, 0, hostNum);

        synGapManager = getSynGapManager(statesManager);
        intraSchedulers = createIntraSchedulers(schedulerNum);

        schedules();

        double endTime = System.currentTimeMillis();
        System.out.println("time: " + (endTime - startTime) / 1000);
        System.out.println("memory: " + MemoryRecord.getMaxUsedMemory() / 1024 / 1024 + "MB");
    }

    private List<IntraScheduler> createIntraSchedulers(int schedulerNum) {
        List<IntraScheduler> intraSchedulers = new ArrayList<>();
        for (int i = 0; i < schedulerNum; i++) {
            IntraScheduler intraScheduler = new IntraSchedulerSimple(i, i, partitionNum);
            intraSchedulers.add(intraScheduler);
        }
        return intraSchedulers;
    }

    private Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> getSelfHostStateMap(StatesManagerSimple statesManagerSimple) {
        try {
            Field privateField = statesManagerSimple.getClass().getDeclaredField("selfHostStateMap");
            privateField.setAccessible(true);
            Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap = (Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>>) privateField.get(statesManagerSimple);
            return selfHostStateMap;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private Map<Integer, TreeMap<Double, Map<Integer, int[]>>> getSynStateMap(StatesManagerSimple statesManagerSimple) {
        try {
            Field privateField = statesManagerSimple.getClass().getDeclaredField("synStateMap");
            privateField.setAccessible(true);
            return (Map<Integer, TreeMap<Double, Map<Integer, int[]>>>) privateField.get(statesManagerSimple);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private SynGapManager getSynGapManager(StatesManagerSimple statesManagerSimple) {
        try {
            Field privateField = statesManagerSimple.getClass().getDeclaredField("synGapManager");
            privateField.setAccessible(true);
            return (SynGapManager) privateField.get(statesManagerSimple);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private int[] getHostStates(StatesManagerSimple statesManagerSimple) {
        try {
            Field privateField = statesManagerSimple.getClass().getDeclaredField("hostStates");
            privateField.setAccessible(true);
            return (int[]) privateField.get(statesManagerSimple);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
//
//        if (changeStateNumPerIntraScheduler == 0){
//        int randoStartIntraSchedulerId = random.nextInt(schedulerNum);
//        for (int i = 0; i < instanceNumPerSynGap; i++) {
//            int intraSchedulerId = (randoStartIntraSchedulerId + i) % schedulerNum;
//            IntraScheduler intraScheduler = intraSchedulers.get(intraSchedulerId);
//            selfHostStateMap.put(intraScheduler, new HashMap<>());
//            int changeStateNumPerPartition = changeStateNumPerIntraScheduler / partitionNum;
//
//        }
//    }
//    private void addSelfStateSynState(List<IntraScheduler> intraSchedulers) {
//        Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap = getSelfHostStateMap(statesManager);
//        int instanceNumPerSynGap = instanceNumPerMillSec * smallSynGap;
//        int changeStateNumPerIntraScheduler = instanceNumPerSynGap / schedulerNum;
//
//        for (IntraScheduler intraScheduler : intraSchedulers) {
//            addSelfStateForIntraScheduler(changeStateNumPerIntraScheduler, intraScheduler, selfHostStateMap);
//        }
//        for (int i = 0; i < instanceNumPerSynGap % schedulerNum; i++) {
//            int intraSchedulerId = random.nextInt(schedulerNum);
//            IntraScheduler intraScheduler = intraSchedulers.get(intraSchedulerId);
//            addSelfStateForIntraScheduler(1, intraScheduler, selfHostStateMap);
//        }
//
//        Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap = getSynStateMap(statesManager);
//        for (int i = 0; i < partitionNum; i++) {
//            synStateMap.put(i, new TreeMap<>());
//            for (int j = 0; j < partitionNum; j++) {
//                double time = synGapManager.getSynTime(j);
//                synStateMap.get(i).put(time, new HashMap<>());
//                int changeStateNumPerPartitionPerSynGap = changeStateNumPerIntraScheduler / partitionNum / partitionNum;
//                for (int k = 0; k < changeStateNumPerPartitionPerSynGap; k++) {
//                    int hostId = random.nextInt(hostNum);
//                    int[] hostState = new int[]{hostCpu, hostRam, hostStorage, hostBw};
//                    synStateMap.get(i).get(time).put(hostId, hostState);
//                }
//            }
//        }
//    }

    private void schedules() {
        Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap = getSelfHostStateMap(statesManager);
        Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap = getSynStateMap(statesManager);
        initSelfMap(selfHostStateMap);

        while (true) {
            double synTime = synGapManager.getSynTime(synGapManager.getPartitionSynCount());
            if (synTime >= allTime) {
                break;
            }

            int num = instanceNumPerMillSec * smallSynGap;
            int numPerIntraScheduler = num / intraSchedulers.size();
            for (IntraScheduler intraScheduler : intraSchedulers) {
                scheduleOnceRoundForIntraScheduler(numPerIntraScheduler, intraScheduler, synTime, selfHostStateMap, synStateMap, getHostStates(statesManager));
            }
            for (int i = 0; i < num % intraSchedulers.size(); i++) {
                int intraSchedulerId = random.nextInt(intraSchedulers.size());
                IntraScheduler intraScheduler = intraSchedulers.get(intraSchedulerId);
                scheduleOnceRoundForIntraScheduler(1, intraScheduler, synTime, selfHostStateMap, synStateMap, getHostStates(statesManager));
            }

            synGapManager.partitionSynGapCountAddOne();
            updateAfterSyn(selfHostStateMap, synStateMap);
        }
    }

    private void initSelfMap(Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap) {
        for (IntraScheduler intraScheduler : intraSchedulers) {
            selfHostStateMap.put(intraScheduler, new HashMap<>());
            for (int i = 0; i < partitionNum; i++) {
                selfHostStateMap.get(intraScheduler).put(i, new HashMap<>());
            }
        }
    }

    private void updateAfterSyn(Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap, Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap) {
        int latestSmallSynGapCount = synGapManager.getPartitionSynCount();
        for (IntraScheduler intraScheduler : intraSchedulers) {
            for (Map<Double, Map<Integer, int[]>> partitionSynStateMap : synStateMap.values()) {
                if (latestSmallSynGapCount >= partitionNum) {
                    partitionSynStateMap.remove(synGapManager.getSynTime(latestSmallSynGapCount - partitionNum));
                }
                partitionSynStateMap.put(synGapManager.getSynTime(latestSmallSynGapCount), new HashMap<>());
            }

            int clearPartitionId = (intraScheduler.getFirstPartitionId() + latestSmallSynGapCount) % partitionNum;
            selfHostStateMap.get(intraScheduler).get(clearPartitionId).clear();
        }
    }

    private void scheduleOnceRoundForIntraScheduler(int num, IntraScheduler intraScheduler, double lastSynTime, Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap, Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap, int[] actualHostStates) {
        MemoryRecord.recordMemory();
        Map<Integer, Integer> partitionLatestSynCount = initPartitionLatestSynCount(intraScheduler);
        for (int i = 0; i < num; i++) {
            int hostId = random.nextInt(hostNum);
            int partitionId = statesManager.getPartitionRangesManager().getPartitionId(hostId);
            int[] hostState = getHostState(random.nextInt(hostNum), selfHostStateMap.get(intraScheduler), synStateMap, actualHostStates, statesManager.getPartitionRangesManager(), partitionLatestSynCount);
            hostState[0] = hostCpu - 1;
            hostState[1] = hostRam - 1;
            hostState[2] = hostStorage - 1;
            hostState[3] = hostBw - 1;

            selfHostStateMap.get(intraScheduler).get(partitionId).put(hostId, hostState);

            if (!synStateMap.get(partitionId).get(lastSynTime).containsKey(hostId)) {
                synStateMap.get(partitionId).get(lastSynTime).put(hostId, hostState);
            }

            System.arraycopy(hostState, 0, actualHostStates, hostId * HostState.STATE_NUM, HostState.STATE_NUM);
        }
        MemoryRecord.recordMemory();
    }

    private Map<Integer, Integer> initPartitionLatestSynCount(IntraScheduler intraScheduler) {
        int latestSynPartitionId = (intraScheduler.getFirstPartitionId() + synGapManager.getPartitionSynCount()) % partitionNum;
        Map<Integer, Integer> partitionLatestSynCount = new HashMap<>();
        for (int partitionId = 0; partitionId < partitionNum; partitionId++) {
            int partDistanceLatestSynPartition = (latestSynPartitionId + partitionNum - partitionId) % partitionNum;
            int partLatestSmallSynGapCount = max(0, synGapManager.getPartitionSynCount() - partDistanceLatestSynPartition);
            partitionLatestSynCount.put(partitionId, partLatestSmallSynGapCount);
        }
        return partitionLatestSynCount;
    }

    private int[] getHostState(int hostId, Map<Integer, Map<Integer, int[]>> selfHostState, Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap,
                               int[] hostStates, PartitionRangesManager partitionRangesManager, Map<Integer, Integer> partitionLatestSynCount) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        if (selfHostState.containsKey(partitionId) && selfHostState.get(partitionId).containsKey(hostId)) {
            return selfHostState.get(partitionId).get(hostId);
        } else {
            TreeMap<Double, Map<Integer, int[]>> partitionSynState = synStateMap.get(partitionId);
            int latestSmallSynCount = partitionLatestSynCount.get(partitionId);
            while (latestSmallSynCount <= synGapManager.getPartitionSynCount()) {
                double synTime = synGapManager.getSynTime(latestSmallSynCount);
                if (partitionSynState.containsKey(synTime) && partitionSynState.get(synTime).containsKey(hostId)) {
                    return partitionSynState.get(synTime).get(hostId);
                }
                latestSmallSynCount++;
            }

            return new int[]{
                    hostStates[hostId * HostState.STATE_NUM],
                    hostStates[hostId * HostState.STATE_NUM + 1],
                    hostStates[hostId * HostState.STATE_NUM + 2],
                    hostStates[hostId * HostState.STATE_NUM + 3]
            };
        }
    }

//    private void addSelfStateSynStateForIntraScheduler(int num, IntraScheduler intraScheduler, Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap, Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap) {
//        selfHostStateMap.putIfAbsent(intraScheduler, new HashMap<>());
//        synStateMap.putIfAbsent(intraScheduler.getId(), new TreeMap<>());
//
//        for (int i = 0; i < num; i++) {
//            int hostId = random.nextInt(hostNum);
//            int[] hostState = new int[]{hostCpu, hostRam, hostStorage, hostBw};
//
//            selfHostStateMap.get(intraScheduler).put(hostId, hostState);
//        }
//    }

//    private void addSelfStateForIntraScheduler(int num, IntraScheduler intraScheduler, Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap) {
//        selfHostStateMap.putIfAbsent(intraScheduler, new HashMap<>());
//        int changeStateNumPerPartition = num / partitionNum;
//        for (int i = 0; i < partitionNum; i++) {
//            addSelfStateForIntraSchedulerPerPartition(changeStateNumPerPartition, i, selfHostStateMap.get(intraScheduler));
//        }
//        for (int i = 0; i < num % partitionNum; i++) {
//            int partitionId = random.nextInt(partitionNum);
//            addSelfStateForIntraSchedulerPerPartition(1, partitionId, selfHostStateMap.get(intraScheduler));
//        }
//    }
//
//    private void addSelfStateForIntraSchedulerPerPartition(int num, int partitionId, Map<Integer, Map<Integer, int[]>> selfHostStateMap) {
//        selfHostStateMap.putIfAbsent(partitionId, new HashMap<>());
//        for (int i = 0; i < num; i++) {
//            int hostId = random.nextInt(hostNum);
//            int[] hostState = new int[]{hostCpu, hostRam, hostStorage, hostBw};
//            selfHostStateMap.get(partitionId).put(hostId, hostState);
//        }
//    }
//
//    private void getAndChangeHostState(int num, List<IntraScheduler> intraSchedulers) {
//        Map<IntraScheduler, Map<Integer, Map<Integer, int[]>>> selfHostStateMap = getSelfHostStateMap(statesManager);
//        Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap = getSynStateMap(statesManager);
//        int[] hostStates = getHostStates(statesManager);
//        MemoryRecord.recordMemory();
//        for (IntraScheduler intraScheduler : intraSchedulers) {
//            getAndChangeSynStateForIntraScheduler(num / intraSchedulers.size(), intraScheduler, selfHostStateMap.get(intraScheduler), synStateMap, hostStates);
//            MemoryRecord.recordMemory();
//        }
//    }
//
//    private void getAndChangeSynStateForIntraScheduler(int num, IntraScheduler intraScheduler, Map<Integer, Map<Integer, int[]>> selfHostState,
//                                                       Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap, int[] hostStates) {
//        Map<Integer, Integer> partitionLatestSynCount = initPartitionLatestSynCount(intraScheduler);
//        for (int i = 0; i < num; i++) {
//            int hostId = random.nextInt(hostNum);
//            int[] hostState = getHostState(hostId, selfHostState, synStateMap, hostStates, statesManager.getPartitionRangesManager(), partitionLatestSynCount);
//
//            int[] newHostState = new int[]{hostCpu - 1, hostRam - 1, hostStorage - 1, hostBw - 1};
//
//            changeHostState(hostId, newHostState, selfHostState, synStateMap, hostStates);
//        }
//    }
//
//
//
//    private void changeHostState(int hostId, int[] newHostState, Map<Integer, Map<Integer, int[]>> selfHostState, Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synStateMap, int[] hostStates) {
//        hostStates[hostId * HostState.STATE_NUM] = newHostState[0];
//        hostStates[hostId * HostState.STATE_NUM + 1] = newHostState[1];
//        hostStates[hostId * HostState.STATE_NUM + 2] = newHostState[2];
//        hostStates[hostId * HostState.STATE_NUM + 3] = newHostState[3];
//
//        int partitionId = statesManager.getPartitionRangesManager().getPartitionId(hostId);
//        if (selfHostState.containsKey(partitionId)) {
//            selfHostState.get(partitionId).put(hostId, newHostState);
//        } else {
//            selfHostState.put(partitionId, new HashMap<>());
//            selfHostState.get(partitionId).put(hostId, newHostState);
//        }
//
//        double synTime = synGapManager.getSynTime(smallSynCountNow);
//        if (synStateMap.containsKey(partitionId)) {
//            if (synStateMap.get(partitionId).containsKey(synTime)) {
//                synStateMap.get(partitionId).get(synTime).put(hostId, newHostState);
//            } else {
//                synStateMap.get(partitionId).put(synTime, new HashMap<>());
//                synStateMap.get(partitionId).get(synTime).put(hostId, newHostState);
//            }
//        } else {
//            synStateMap.put(partitionId, new TreeMap<>());
//            synStateMap.get(partitionId).put(synTime, new HashMap<>());
//            synStateMap.get(partitionId).get(synTime).put(hostId, newHostState);
//        }
//    }
//
//    private void allocateInstances(IntraScheduler intraScheduler, Instance instance, int instanceNum) {
//        SynState synStateSimple = statesManager.getSynState(intraScheduler);
//        for (int i = 0; i < instanceNum; i++) {
//            allocateInstance(intraScheduler, instance, i);
//        }
//    }
//
//    private void allocateInstance(IntraScheduler intraScheduler, Instance instance, int hostId) {
//
//    }
}
