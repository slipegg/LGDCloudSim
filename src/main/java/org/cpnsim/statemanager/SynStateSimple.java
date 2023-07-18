package org.cpnsim.statemanager;

import lombok.Getter;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;

public class SynStateSimple implements SynState {
    @Getter
    //partitionId, time, hostId, hostState
    Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState;
    int[] nowHostStates;
    PartitionRangesManager partitionRangesManager;
    @Getter
    Map<Integer, Map<Integer, int[]>> selfHostState;
    InnerScheduler scheduler;
    StatesManagerSimple statesManagerSimple;
    Datacenter datacenter;
    int smallSynNum;
    double smallSynGap;
    int latestSynPartitionId;
    Map<Integer,Double> partitionLatestSynTime = new HashMap<>();
    Map<Integer,Double> partitionOldestSynTime = new HashMap<>();
    double nowTime;

    public SynStateSimple(StatesManagerSimple statesManagerSimple, Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState, int[] nowHostStates, PartitionRangesManager partitionRangesManager, Map<Integer, Map<Integer, int[]>> selfHostState, InnerScheduler scheduler) {
        this.synState = synState;
        this.nowHostStates = nowHostStates;
        this.partitionRangesManager = partitionRangesManager;
        this.selfHostState = selfHostState;
        this.scheduler = scheduler;
        this.statesManagerSimple = statesManagerSimple;
        this.datacenter = statesManagerSimple.getDatacenter();
        this.smallSynGap = statesManagerSimple.smallSynGap;
        this.nowTime = datacenter.getSimulation().clock();
        this.smallSynNum = (int) (nowTime / smallSynGap);
        this.latestSynPartitionId = (scheduler.getFirstPartitionId() + smallSynNum) % partitionRangesManager.getPartitionNum();

        for(int partitionId : partitionRangesManager.getPartitionIds()){
            double latestSynTime = max(0.0, statesManagerSimple.smallSynGap * (smallSynNum - (latestSynPartitionId + partitionRangesManager.getPartitionNum() - partitionId) % partitionRangesManager.getPartitionNum()));//TODO 有问题，如果分区和时间不能被整除应该要处理
            double oldestSynTime = max(0.0, latestSynTime - statesManagerSimple.synGap * (statesManagerSimple.predictRecordNum - 1));
            partitionLatestSynTime.put(partitionId,latestSynTime);
            partitionOldestSynTime.put(partitionId,oldestSynTime);
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
        List<HostStateHistory> hostStateHistories = new ArrayList<>();
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        TreeMap<Double,Map<Integer,int[]>> partitionSynState = synState.get(partitionId);
        double synTime = partitionLatestSynTime.get(partitionId);
        double oldTime = partitionOldestSynTime.get(partitionId);
        for (double time : partitionSynState.keySet()) {
            if (time >= oldTime && partitionSynState.get(time).containsKey(hostId)) {
                hostStateHistories.add(new HostStateHistory(partitionSynState.get(time).get(hostId), time));
                do {
                    oldTime += statesManagerSimple.synGap;
                } while (time >= oldTime);
            }
            if (oldTime > synTime) {
                break;
            }
        }
        if (hostStateHistories.size() == 0) {
            return null;
        } else {
            return statesManagerSimple.predictionManager.predictHostState(hostStateHistories);
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
        if (!statesManagerSimple.predictable) {
            hostState = getSynHostState(hostId);
        } else {
            hostState = getPredictSynState(hostId);
        }
        if (hostState == null) {
            return nowHostStates[hostId * HostState.STATE_NUM] >= instance.getCpu() && nowHostStates[hostId * HostState.STATE_NUM + 1] >= instance.getRam() && nowHostStates[hostId * HostState.STATE_NUM + 2] >= instance.getStorage() && nowHostStates[hostId * HostState.STATE_NUM + 3] >= instance.getBw();
        } else {
            return hostState[0] >= instance.getCpu() && hostState[1] >= instance.getRam() && hostState[2] >= instance.getStorage() && hostState[3] >= instance.getBw();
        }
    }

    @Override
    public void allocateTmpResource(int hostId, Instance instance) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        if (selfHostState.get(partitionId).containsKey(hostId)) {
            int[] hostState = selfHostState.get(partitionId).get(hostId);
            hostState[0] -= instance.getCpu();
            hostState[1] -= instance.getRam();
            hostState[2] -= instance.getStorage();
            hostState[3] -= instance.getBw();
        } else
            {
                int[] hostState = getSynHostState(hostId);
                if (hostState != null){
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
