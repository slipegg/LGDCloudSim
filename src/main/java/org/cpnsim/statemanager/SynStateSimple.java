package org.cpnsim.statemanager;

import lombok.Getter;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;
import static org.apache.commons.lang3.math.NumberUtils.min;

public class SynStateSimple implements SynState {
    @Getter
    Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState;
    int[] nowHostStates;
    PartitionRangesManager partitionRangesManager;
    PredictionManager predictionManager;
    @Getter
    Map<Integer, Map<Integer, int[]>> selfHostState;
    InnerScheduler scheduler;
    double smallSynGap;
    double synGap;
    int latestSynPartitionId;
    Map<Integer, Double> partitionLatestSynTime = new HashMap<>();
    Map<Integer, Double> partitionOldestSynTime = new HashMap<>();
    Map<Integer, int[]> predictHostStateMap = new HashMap<>();
    double nowTime;
    boolean predictable;

    public SynStateSimple(Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState, int[] nowHostStates,
                          PartitionRangesManager partitionRangesManager, Map<Integer, Map<Integer, int[]>> selfHostState, InnerScheduler scheduler,
                          PredictionManager predictionManager, double nowTime, double smallSynGap, double synGap, int predictRecordNum, boolean predictable) {
        this.synState = synState;
        this.nowHostStates = nowHostStates;
        this.partitionRangesManager = partitionRangesManager;
        this.selfHostState = selfHostState;
        this.scheduler = scheduler;
        this.predictionManager = predictionManager;
        this.smallSynGap = smallSynGap;
        this.synGap = synGap;
        this.predictable = predictable;
        int smallSynNum = (int) (nowTime / smallSynGap);
        this.latestSynPartitionId = (scheduler.getFirstPartitionId() + smallSynNum) % partitionRangesManager.getPartitionNum();

        for (int partitionId : partitionRangesManager.getPartitionIds()) {
            double latestSynTime = max(0.0, smallSynGap * (smallSynNum - (latestSynPartitionId + partitionRangesManager.getPartitionNum() - partitionId) % partitionRangesManager.getPartitionNum()));//TODO 有问题，如果分区和时间不能被整除应该要处理
            double oldestSynTime = latestSynTime - synGap * (min(latestSynTime / synGap, predictRecordNum - 1));
            partitionLatestSynTime.put(partitionId, latestSynTime);
            partitionOldestSynTime.put(partitionId, oldestSynTime);
        }
    }

    private int[] getSynHostState(int hostId) {
        if(smallSynGap == 0){
            return null;
        }
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        TreeMap<Double,Map<Integer,int[]>> partitionSynState = synState.get(partitionId);
        double synTime = partitionLatestSynTime.get(partitionId);
        while(synTime<=nowTime){
            if(partitionSynState.containsKey(synTime) && partitionSynState.get(synTime).containsKey(hostId)){
                return partitionSynState.get(synTime).get(hostId);
            }
            synTime += smallSynGap;
        }
        return null;
    }

    private int[] getPredictSynState(int hostId) {
        if (smallSynGap == 0) {
            return null;
        }
        if (predictHostStateMap.containsKey(hostId)) {
            return predictHostStateMap.get(hostId);
        }
        List<HostStateHistory> hostStateHistories = new ArrayList<>();
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        TreeMap<Double, Map<Integer, int[]>> partitionSynState = synState.get(partitionId);
        double synTime = partitionLatestSynTime.get(partitionId);
        double oldTime = partitionOldestSynTime.get(partitionId);
        for (double time : partitionSynState.keySet()) {
            if (time >= oldTime && partitionSynState.get(time).containsKey(hostId)) {
                hostStateHistories.add(new HostStateHistory(partitionSynState.get(time).get(hostId), time));
                do {
                    oldTime += synGap;
                } while (time >= oldTime);
            }
            if (oldTime > synTime) {
                break;
            }
        }
        if (hostStateHistories.size() == 0) {
            return null;
        } else {
            int[] predictHostState = predictionManager.predictHostState(hostStateHistories);
            predictHostStateMap.put(hostId, predictHostState);
            return predictHostState;
        }
    }

    @Override
    public boolean isSuitable(int hostId, Instance instance) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        int[] hostState;
        //在自己维护的状态表中
        if (selfHostState.get(partitionId).containsKey(hostId)) {
            hostState = selfHostState.get(partitionId).get(hostId);
            return hostState[0] >= instance.getCpu() && hostState[1] >= instance.getRam() && hostState[2] >= instance.getStorage() && hostState[3] >= instance.getBw();
        }
        if (predictable) {
            hostState = getPredictSynState(hostId);
        } else {
            hostState = getSynHostState(hostId);
        }
        if (hostState == null) {
            return nowHostStates[hostId * HostState.STATE_NUM] >= instance.getCpu() && nowHostStates[hostId * HostState.STATE_NUM + 1] >= instance.getRam() && nowHostStates[hostId * HostState.STATE_NUM + 2] >= instance.getStorage() && nowHostStates[hostId * HostState.STATE_NUM + 3] >= instance.getBw();
        } else {
            return hostState[0] >= instance.getCpu() && hostState[1] >= instance.getRam() && hostState[2] >= instance.getStorage() && hostState[3] >= instance.getBw();
        }
    }

    @Override
    public void allocateTmpResource(int hostId, Instance instance) {
        int[] hostState;
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        if (selfHostState.get(partitionId).containsKey(hostId)) {
            hostState = selfHostState.get(partitionId).get(hostId);
            hostState[0] -= instance.getCpu();
            hostState[1] -= instance.getRam();
            hostState[2] -= instance.getStorage();
            hostState[3] -= instance.getBw();
        } else {
            if (predictable) {
                hostState = getPredictSynState(hostId);
            } else {
                hostState = getSynHostState(hostId);
            }
            if (hostState != null) {
                selfHostState.get(partitionId).put(hostId, new int[]{
                        hostState[0] - instance.getCpu(),
                        hostState[1] - instance.getRam(),
                        hostState[2] - instance.getStorage(),
                        hostState[3] - instance.getBw()
                });
            } else {
                selfHostState.get(partitionId).put(hostId, new int[]{
                        nowHostStates[hostId * HostState.STATE_NUM] - instance.getCpu(),
                            nowHostStates[hostId * HostState.STATE_NUM + 1] - instance.getRam(),
                            nowHostStates[hostId * HostState.STATE_NUM + 2] - instance.getStorage(),
                            nowHostStates[hostId * HostState.STATE_NUM + 3] - instance.getBw()
                    });
            }
        }
    }
}
