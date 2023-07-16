package org.cpnsim.statemanager;

import lombok.Getter;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    public SynStateSimple(StatesManagerSimple statesManagerSimple, Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState, int[] nowHostStates, PartitionRangesManager partitionRangesManager, Map<Integer, Map<Integer, int[]>> selfHostState, InnerScheduler scheduler) {
        this.synState = synState;
        this.nowHostStates = nowHostStates;
        this.partitionRangesManager = partitionRangesManager;
        this.statesManagerSimple = statesManagerSimple;
        this.selfHostState = selfHostState;
        this.scheduler = scheduler;
        this.datacenter = statesManagerSimple.getDatacenter();
    }

    private int[] getSynHostState(int hostId) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        int smallSynNum = (int) (datacenter.getSimulation().clock() / statesManagerSimple.smallSynGap);
        int synPartitionId = (scheduler.getFirstPartitionId() + smallSynNum) % partitionRangesManager.getPartitionNum();
        double synTime = max(0.0, statesManagerSimple.smallSynGap * (smallSynNum - (synPartitionId + partitionRangesManager.getPartitionNum() - partitionId)));//TODO 有问题，如果分区和时间不能被整除应该要处理
        for (double time : synState.get(synPartitionId).keySet()) {
            if (time >= synTime && synState.get(synPartitionId).get(time).containsKey(hostId)) {
                return synState.get(synPartitionId).get(time).get(hostId);
            }
        }
        return null;
    }

    private int[] getPredictSynState(int hostId) {
        List<HostStateHistory> hostStateHistories = new ArrayList<>();
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        int smallSynNum = (int) (datacenter.getSimulation().clock() / statesManagerSimple.smallSynGap);
        int synPartitionId = (scheduler.getFirstPartitionId() + smallSynNum) % partitionRangesManager.getPartitionNum();
        double synTime = max(0.0, statesManagerSimple.smallSynGap * (smallSynNum - (synPartitionId + partitionRangesManager.getPartitionNum() - partitionId)));//TODO 有问题，如果分区和时间不能被整除应该要处理
        double predictTime = synTime - statesManagerSimple.synGap * (statesManagerSimple.predictRecordNum - 1);
        for (double time : synState.get(synPartitionId).keySet()) {
            if (time >= predictTime && synState.get(synPartitionId).get(time).containsKey(hostId)) {
                hostStateHistories.add(new HostStateHistory(synState.get(synPartitionId).get(time).get(hostId), time));
                do {
                    predictTime += statesManagerSimple.synGap;
                } while (time >= predictTime);
            }
            if (predictTime > synTime) {
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
        } else if (synState.get(partitionId).containsKey(hostId)) {
            int[] hostState = getSynHostState(hostId);
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
