package org.lgdcloudsim.statemanager;

import lombok.Getter;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.request.Instance;

import java.util.*;

import static org.apache.commons.lang3.math.NumberUtils.max;
import static org.apache.commons.lang3.math.NumberUtils.min;

/**
 * This class implements the interface {@link SynState}.
 * For SynState, its state representation has 3 layers.
 * <ul>
 *     <li>selfHostState: The state of the hosts that the intra-scheduler have scheduled instances to,
 *     so that the intra-scheduler need to record the state of the allocated hosts in its view to the selfHostState.</li>
 *     <li>synState: The state of the hosts in different partition synchronization time.
 *     Note the it will only record the states of the changed hosts during the partition synchronization time. For more details, see {@link StatesManagerSimple}.</li>
 *     <li>nowHostStates: The actual state of all hosts in the datacenter.</li>
 * </ul>
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

public class SynStateSimple implements SynState {
    /**
     * When scheduling, it will pretend that the previous scheduling result is successful,
     * so the ideal state of the scheduled host will be saved in this dictionary,
     * the purpose is not to modify other data.
     **/

    @Getter
    Map<Integer, Map<Integer, int[]>> selfHostState;

    /**
     * The host status here refers to the host status obtained during synchronization
     **/
    @Getter
    Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState;

    /**
     * The actual state of all hosts in the datacenter
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

    /**
     * see {@link SynGapManager}
     */
    SynGapManager synGapManager;

    /**
     * The constructor of the class SynStateSimple.
     *
     * @param synState               the state of the hosts in different partition synchronization time.
     * @param nowHostStates          the actual state of all hosts in the datacenter.
     * @param partitionRangesManager the partition ranges manager.
     * @param selfHostState          the state of the hosts that the intra-scheduler have scheduled instances to.
     * @param scheduler              the intra-scheduler.
     * @param predictionManager      the prediction manager.
     * @param synGapManager          the syn gap manager.
     * @param predictRecordNum       the record data num for predicting.
     * @param predictable            whether to use the prediction function.
     */
    public SynStateSimple(Map<Integer, TreeMap<Double, Map<Integer, int[]>>> synState, int[] nowHostStates,
                          PartitionRangesManager partitionRangesManager, Map<Integer, Map<Integer, int[]>> selfHostState, IntraScheduler scheduler,
                          PredictionManager predictionManager, SynGapManager synGapManager, int predictRecordNum, boolean predictable) {
        this.synState = synState;
        this.nowHostStates = nowHostStates;
        this.partitionRangesManager = partitionRangesManager;
        this.selfHostState = selfHostState;
        this.predictionManager = predictionManager;
        this.synGapManager = synGapManager;
        this.predictable = predictable;
        this.latestSynPartitionId = (scheduler.getFirstPartitionId() + synGapManager.getPartitionSynCount()) % partitionRangesManager.getPartitionNum();

        for (int partitionId : partitionRangesManager.getPartitionIds()) {
            int partDistanceLatestSynPartition = (latestSynPartitionId + partitionRangesManager.getPartitionNum() - partitionId) % partitionRangesManager.getPartitionNum();
            int partLatestSmallSynGapCount = max(0, synGapManager.getPartitionSynCount() - partDistanceLatestSynPartition);
            partitionLatestSynCount.put(partitionId, partLatestSmallSynGapCount);

            int additionRecordNum = 0;
            if (predictable) {
                additionRecordNum = min(partLatestSmallSynGapCount / partitionRangesManager.getPartitionNum(), predictRecordNum - 1);
            }
            int partOldSmallSynGapCount = partLatestSmallSynGapCount - partitionRangesManager.getPartitionNum() * additionRecordNum;
            partitionOldSynCount.put(partitionId, partOldSmallSynGapCount);
        }
    }

    /**
     * When looking for the host state, we need to check whether there is relevant host data in selfHostState,
     * and then try to call getSynHostState to obtain synhost.
     * If there is still no, it means that the host state has not changed.
     * We go to hostState to obtain the corresponding Host Status.
     * @param hostId the id of the host.
     * @return the host state.
     */
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
     *
     * @param hostId   the id of the host
     * @param instance the instance to be placed
     * @return true if the instance can be placed in the hostId host, otherwise false
     */
    @Override
    public boolean isSuitable(int hostId, Instance instance) {
        HostState hostState = getHostState(hostId);
        return hostState.isSuitable(instance);
    }

    /**
     * When the scheduler decides to schedule the instance to a certain host,
     * this function needs to be called to update the scheduled host state to selfHostState.
     * Note that the instance may fail to be scheduled.
     * In this case, we need to call {@link StatesManager#revertSelfHostState} to modify the status back.
     *
     * @param hostId  the id of the host
     * @param instance the instance to be placed
     */
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
        while (latestSmallSynCount <= synGapManager.getPartitionSynCount()) {
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
     * Because we have the latest host state,we don't need to predict the host state.
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
        while (tmpCount <= synGapManager.getPartitionSynCount()) {
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