package org.oldexample.StatesManagerComparison;

import lombok.Getter;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.intrascheduler.IntraSchedulerSimple;
import org.lgdcloudsim.record.MemoryRecord;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.PartitionRangesManager;
import org.lgdcloudsim.statemanager.SynGapManager;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;

public class ComparedStatesManagerCopy {
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
    StatesMangerCopy statesManager;
    Random random = new Random(1);
    //    int smallSynCountNow = 0;
    SynGapManager synGapManager;
    List<IntraScheduler> intraSchedulers;

    class StatesMangerCopy {
        int[] hostStates;

        Map<IntraScheduler, Map<Integer, int[]>> selfHostStateMap;

        @Getter
        PartitionRangesManager partitionRangesManager;

        @Getter
        SynGapManager synGapManager;

        public StatesMangerCopy(int hostNum, PartitionRangesManager partitionRangesManager, List<IntraScheduler> intraSchedulers, double synGap) {
            hostStates = new int[hostNum * HostState.STATE_NUM];
            this.partitionRangesManager = partitionRangesManager;
            this.synGapManager = new SynGapManager(synGap, partitionRangesManager.getPartitionNum());
            selfHostStateMap = new HashMap<>();
            for (IntraScheduler intraScheduler : intraSchedulers) {
                selfHostStateMap.put(intraScheduler, new HashMap<>());
                for (int i = 0; i < partitionRangesManager.getPartitionNum(); i++) {
                    selfHostStateMap.get(intraScheduler).put(i, getPartitionHostStatesCopy(i));
                }
            }
        }

        private int[] getPartitionHostStatesCopy(int partitionId) {
            int[] range = partitionRangesManager.getRange(partitionId);
            int[] partitionCopy = new int[(range[1] - range[0] + 1) * HostState.STATE_NUM];
            System.arraycopy(hostStates, range[0] * HostState.STATE_NUM, partitionCopy, 0, partitionCopy.length);
            return partitionCopy;
        }

        private void partitionSyn() {
            synGapManager.partitionSynGapCountAddOne();
            for (IntraScheduler intraScheduler : intraSchedulers) {
                int synPartitionId = (intraScheduler.getFirstPartitionId() + synGapManager.getPartitionSynCount()) % partitionRangesManager.getPartitionNum();
                selfHostStateMap.get(intraScheduler).put(synPartitionId, getPartitionHostStatesCopy(synPartitionId));
            }
        }

        private int[] getHostState(int hostId, IntraScheduler intraScheduler) {
            int partitionId = partitionRangesManager.getPartitionId(hostId);
            int[] partitionHostStates = selfHostStateMap.get(intraScheduler).get(partitionId);
            int[] hostState = new int[HostState.STATE_NUM];
            System.arraycopy(partitionHostStates, (hostId - partitionRangesManager.getRange(partitionId)[0]) * HostState.STATE_NUM, hostState, 0, HostState.STATE_NUM);
            return hostState;
        }

        private void changeHostState(int hostId, int[] newHostState, IntraScheduler intraScheduler) {
            int partitionId = partitionRangesManager.getPartitionId(hostId);
            int[] partitionHostStates = selfHostStateMap.get(intraScheduler).get(partitionId);
            System.arraycopy(newHostState, 0, partitionHostStates, (hostId - partitionRangesManager.getRange(partitionId)[0]) * HostState.STATE_NUM, HostState.STATE_NUM);
            System.arraycopy(newHostState, 0, hostStates, hostId * HostState.STATE_NUM, HostState.STATE_NUM);
        }
    }

    public static void main(String[] args) {
        new ComparedStatesManagerCopy();
    }

    public ComparedStatesManagerCopy() {
        double startTime = System.currentTimeMillis();

        PartitionRangesManager partitionRangesManager = new PartitionRangesManager();
        partitionRangesManager.setAverageCutting(0, hostNum - 1, 100);
        List<IntraScheduler> intraSchedulers = createIntraSchedulers(schedulerNum);
        statesManager = new StatesMangerCopy(hostNum, partitionRangesManager, intraSchedulers, synGap);
        synGapManager = statesManager.getSynGapManager();

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

    private void schedules() {
        while (true) {
            double synTime = synGapManager.getSynTime(synGapManager.getPartitionSynCount());
            if (synTime >= allTime) {
                break;
            }

            int num = instanceNumPerMillSec * smallSynGap;
            int numPerIntraScheduler = num / intraSchedulers.size();
            for (IntraScheduler intraScheduler : intraSchedulers) {
                scheduleOnceRoundForIntraScheduler(numPerIntraScheduler, intraScheduler);
            }
            for (int i = 0; i < num % intraSchedulers.size(); i++) {
                int intraSchedulerId = random.nextInt(intraSchedulers.size());
                IntraScheduler intraScheduler = intraSchedulers.get(intraSchedulerId);
                scheduleOnceRoundForIntraScheduler(1, intraScheduler);
            }

            statesManager.partitionSyn();
        }
    }

    private void scheduleOnceRoundForIntraScheduler(int num, IntraScheduler intraScheduler) {
        MemoryRecord.recordMemory();

        for (int i = 0; i < num; i++) {
            int hostId = random.nextInt(hostNum);
            int[] hostState = statesManager.getHostState(hostId, intraScheduler);
            hostState[0] = hostCpu - 1;
            hostState[1] = hostRam - 1;
            hostState[2] = hostStorage - 1;
            hostState[3] = hostBw - 1;

            statesManager.changeHostState(hostId, hostState, intraScheduler);
        }

        MemoryRecord.recordMemory();
    }
}
