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
     * The id of the last partition to synchronize
     **/
    int latestSynPartitionId;

    /**
     * The small syn count when the state was last synced for each region
     **/

    Map<Integer, Integer> partitionLatestSynCount = new HashMap<>();

    /**
     * Earliest recorded small syn count for each partition, which is used by the {@link PredictionManager}
     **/
    Map<Integer, Integer> partitionOldSynCount = new HashMap<>();

    /**
     * The host status has been predicted, the purpose of this data is to prevent repeated predictions
     **/
    Map<Integer, int[]> predictHostStateMap = new HashMap<>();

    /**
     * Whether to enable prediction
     **/
    boolean predictable;

    SynGapManager synGapManager;

    public SynStateSimple(Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState, int[] nowHostStates,
                          PartitionRangesManager partitionRangesManager, Map<Integer, Map<Integer, int[]>> selfHostState, InnerScheduler scheduler,
                          PredictionManager predictionManager, SynGapManager synGapManager, int predictRecordNum, boolean predictable) {
        this.synState = synState;
        this.nowHostStates = nowHostStates;
        this.partitionRangesManager = partitionRangesManager;
        this.selfHostState = selfHostState;
        this.predictionManager = predictionManager;
        this.synGapManager = synGapManager;
        this.predictable = predictable;
        this.latestSynPartitionId = (scheduler.getFirstPartitionId() + synGapManager.getSmallSynGapCount()) % partitionRangesManager.getPartitionNum();

        for (int partitionId : partitionRangesManager.getPartitionIds()) {
            int partDistanceLatestSynPartition = (latestSynPartitionId + partitionRangesManager.getPartitionNum() - partitionId) % partitionRangesManager.getPartitionNum();
            int partLatestSmallSynGapCount = max(0, synGapManager.getSmallSynGapCount() - partDistanceLatestSynPartition);
            partitionLatestSynCount.put(partitionId, partLatestSmallSynGapCount);

            int additionRecordNum = 0;
            if (predictable) {
                additionRecordNum = min(partLatestSmallSynGapCount / partitionRangesManager.getPartitionNum(), predictRecordNum - 1);
            }
            int partOldSmallSynGapCount = partLatestSmallSynGapCount - partitionRangesManager.getPartitionNum() * additionRecordNum;
            partitionOldSynCount.put(partitionId, partOldSmallSynGapCount);
        }
    }

    @Override
    public HostState getHostState(int hostId){
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        if (selfHostState.get(partitionId).containsKey(hostId)) {
            return new HostState(selfHostState.get(partitionId).get(hostId));
        }else{
            int[] hostState;
            if (predictable) {
                hostState = getPredictSynState(hostId);
            } else {
                hostState = getSynHostState(hostId);
            }

            if (hostState == null) {
                return new HostState(nowHostStates[hostId * HostState.STATE_NUM], nowHostStates[hostId * HostState.STATE_NUM + 1], nowHostStates[hostId * HostState.STATE_NUM + 2], nowHostStates[hostId * HostState.STATE_NUM + 3]);
            } else {
                return new HostState(hostState);
            }
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
//        int partitionId = partitionRangesManager.getPartitionId(hostId);
//        int[] hostState;
//        if (selfHostState.get(partitionId).containsKey(hostId)) {
//            hostState = selfHostState.get(partitionId).get(hostId);
//            return hostState[0] >= instance.getCpu() && hostState[1] >= instance.getRam() && hostState[2] >= instance.getStorage() && hostState[3] >= instance.getBw();
//        }
//        if (predictable) {
//            hostState = getPredictSynState(hostId);
//        } else {
//            hostState = getSynHostState(hostId);
//        }
//        if (hostState == null) {
//            return nowHostStates[hostId * HostState.STATE_NUM] >= instance.getCpu() && nowHostStates[hostId * HostState.STATE_NUM + 1] >= instance.getRam() && nowHostStates[hostId * HostState.STATE_NUM + 2] >= instance.getStorage() && nowHostStates[hostId * HostState.STATE_NUM + 3] >= instance.getBw();
//        } else {
//            return hostState[0] >= instance.getCpu() && hostState[1] >= instance.getRam() && hostState[2] >= instance.getStorage() && hostState[3] >= instance.getBw();
//        }

        HostState hostState = getHostState(hostId);
        return hostState.isSuitable(instance);
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
        if (!synGapManager.isSynCostTime()) {
            return null;
        }
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        TreeMap<Double, Map<Integer, int[]>> partitionSynState = synState.get(partitionId);
        //TODO 这里需要再细看一下
        int latestSmallSynCount = partitionLatestSynCount.get(partitionId);
        while (latestSmallSynCount <= synGapManager.getSmallSynGapCount()) {
            double synTime = synGapManager.getSynTime(latestSmallSynCount);
            if (partitionSynState.containsKey(synTime) && partitionSynState.get(synTime).containsKey(hostId)) {
                return partitionSynState.get(synTime).get(hostId);
            }
            latestSmallSynCount++;
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
        if (!synGapManager.isSynCostTime()) {
            return null;
        }
        if (predictHostStateMap.containsKey(hostId)) {
            return predictHostStateMap.get(hostId);
        }
        List<HostStateHistory> hostStateHistories = new ArrayList<>();
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        TreeMap<Double, Map<Integer, int[]>> partitionSynState = synState.get(partitionId);
        int latestSmallSynCount = partitionLatestSynCount.get(partitionId);
        int oldSmallSynCount = partitionOldSynCount.get(partitionId);
        int tmpCount = oldSmallSynCount;
        while (tmpCount <= synGapManager.getSmallSynGapCount()) {
            if (tmpCount >= oldSmallSynCount) {
                double time = synGapManager.getSynTime(tmpCount);
                if (partitionSynState.containsKey(time) && partitionSynState.get(time).containsKey(hostId)) {
                    hostStateHistories.add(new HostStateHistory(partitionSynState.get(time).get(hostId), time));
                }
                do {
                    oldSmallSynCount += partitionRangesManager.getPartitionNum();
                } while (tmpCount >= oldSmallSynCount);
                if (oldSmallSynCount > latestSmallSynCount) {
                    break;
                }
            }
            tmpCount++;
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