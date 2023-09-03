package org.cpnsim.statemanager;

import lombok.Getter;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;
import static org.apache.commons.lang3.math.NumberUtils.min;

/**
 * This class implements the interface {@link SynState}.
 * For SynState, its state representation has 3 layers.
 * First, a selfHostState is maintained to represent the state of a certain host from the perspective of the scheduler.
 * Second, it also maintains a host state that is synchronized by itself
 * Third, a real host state of all hosts will be retained
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */

public class SynStateSimple implements SynState {
    /**
     * The host status here refers to the host status obtained during synchronization
     **/
    @Getter
    Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState;

    /**
     * The latest host status at the current moment
     **/
    int[] nowHostStates;

    /**
     * see {@link  PartitionRangesManager}
     **/
    PartitionRangesManager partitionRangesManager;

    /**
     * see {@link  PredictionManager}
     **/
    PredictionManager predictionManager;

    /**
     * When scheduling, it will pretend that the previous scheduling result is successful,
     * so the ideal state of the scheduled host will be saved in this dictionary,
     * the purpose is not to modify other data
     **/
    @Getter
    Map<Integer, Map<Integer, int[]>> selfHostState;

    /**
     * see {@link  InnerScheduler}
     **/
    InnerScheduler scheduler;

    /**
     * The time interval between two synchronization points for a {@link InnerScheduler}
     **/
    double smallSynGap;

    /**
     * The time it takes for a {@link InnerScheduler} to synchronize all hosts in the datacenter
     **/
    double synGap;

    /**
     * The id of the last partition to synchronize
     **/
    int latestSynPartitionId;

    /**
     * The time when the state was last synced for each region
     **/
    Map<Integer, Double> partitionLatestSynTime = new HashMap<>();

    /**
     * Earliest recorded time for each partition, which is used by the {@link PredictionManager}
     **/
    Map<Integer, Double> partitionOldestSynTime = new HashMap<>();

    /**
     * The host status has been predicted, the purpose of this data is to prevent repeated predictions
     **/
    Map<Integer, int[]> predictHostStateMap = new HashMap<>();

    /**
     * Current moment
     **/
    double nowTime;

    /**
     * Whether to enable prediction
     **/
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

    /**
     * Determine whether this instance is suitable for placement in a hostId host.
     * When looking for the host state, we need to check whether there is relevant host data in selfHostState,
     * and then try to call getSynHostState to obtain synhost.
     * If there is still no, it means that the host state has not changed.
     * We go to hostState to obtain the corresponding Host Status.
     *
     * @param hostId   the id of the host
     * @param instance the instance to be placed
     * @return true if the instance can be placed in the hostId host, otherwise false
     */
    @Override
    public boolean isSuitable(int hostId, Instance instance) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        int[] hostState;
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

    /**
     * Get the state of the host when synchronized.
     *
     * @param hostId the host id to get the state
     * @return the state of the host when synchronized
     */
    private int[] getSynHostState(int hostId) {
        if (smallSynGap == 0) {
            return null;
        }
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        TreeMap<Double, Map<Integer, int[]>> partitionSynState = synState.get(partitionId);
        //TODO 这里需要再细看一下
        double synTime = partitionLatestSynTime.get(partitionId);
        while (synTime <= nowTime) {
            if (partitionSynState.containsKey(synTime) && partitionSynState.get(synTime).containsKey(hostId)) {
                return partitionSynState.get(synTime).get(hostId);
            }
            synTime += smallSynGap;
        }
        return null;
    }

    /**
     * Get the predicted host state.
     * Note that if the smallSynGap is 0, the predicted host state is null.
     * Because we have the lastest host state,we don't need to predict the host state.
     * It will use the data stored in synState for a period of time to make predictions.
     *
     * @param hostId the host id to predict
     * @return the predicted host state
     */
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
}