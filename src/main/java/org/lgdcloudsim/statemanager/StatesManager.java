package org.lgdcloudsim.statemanager;

import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.datacenter.DatacenterPowerOnRecord;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.intrascheduler.IntraSchedulerResult;
import org.lgdcloudsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An interface to manage the states of the datacenter.
 * It records all host states in the datacenter, the partition information, and is responsible for allocating instances and releasing resources occupied by instances.
 * It also provides the ability to synchronize the state to the intra-scheduler and inter-scheduler.
 * To synchronize the state to the intra-scheduler, it records the changed host states during the synchronization gap and the independent scheduled host states of every intra-scheduler.
 * To synchronize the state to the inter-scheduler, it records the simple overall state information of hosts in an entire datacenter, see {@link SimpleState}.
 * Additionally, it provides the ability to predict the future state of the datacenter, see {@link PredictionManager}.
 * It also provides the ability to record the power-on time of the hosts in the datacenter, see {@link DatacenterPowerOnRecord}.
 * It also manages the schedule view of every intra-scheduler.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface StatesManager {
    /**
     * Logger for this class.
     */
    Logger LOGGER = LoggerFactory.getLogger(StatesManager.class.getSimpleName());

    /**
     * Get the datacenter it belongs to.
     *
     * @return the datacenter it belongs to.
     */
    Datacenter getDatacenter();

    /**
     * Set the datacenter it belongs to.
     * @param datacenter the datacenter it belongs to.
     * @return the StatesManager itself.
     */
    StatesManager setDatacenter(Datacenter datacenter);

    /**
     * Get the host states in the datacenter.
     * @param cpu the cpu capacity of the host.
     * @param ram the ram capacity of the host.
     * @param storage the storage capacity of the host.
     * @param bw the bw capacity of the host.
     * @param startId the start id of the host.
     * @param length the number of hosts that has the same capacity.
     * @return the StatesManager itself.
     */
    StatesManager initHostStates(int cpu, int ram, int storage, int bw, int startId, int length);

    /**
     * Initialization the host state with {@link HostStateGenerator}.
     * @param hostStateGenerator the host state generator.
     * @return the StatesManager itself.
     */
    StatesManager initHostStates(HostStateGenerator hostStateGenerator);

    /**
     * Get the {@link PartitionRangesManager}.
     * @return the partition ranges manager.
     */
    PartitionRangesManager getPartitionRangesManager();

    /**
     * Get the host num in all datacenter.
     * @return the host num in all datacenter.
     */
    int getHostNum();

    /**
     * Get the host status at the current moment according to the host id.
     * @param hostId the id of the host.
     * @return the host state at the current moment.
     */
    HostState getActualHostState(int hostId);

    /**
     * Get the host status maintained by the center state manager according to the host id.
     *
     * @param hostId the id of the host.
     * @return the host state maintained by the center state manager.
     */
    HostState getCenterHostState(int hostId);

    /**
     * Allocate the instance on the host with hostId.
     *
     * @param hostId   the id of the host.
     * @param instance the instance to be allocated.
     * @return whether the instance is allocated successfully.
     */
    boolean allocate(int hostId, Instance instance);

    /**
     * Allocate the instance on the host which is recorded in the instance's expectedScheduleHostId.
     * @param instance the instance to be allocated.
     * @return whether the instance is allocated successfully.
     */
    boolean allocate(Instance instance);

    /**
     * Release the instance from the host with hostId.
     *
     * @param hostId   the id of the host.
     * @param instance the instance to be released.
     * @return the StatesManager itself.
     */
    StatesManager release(int hostId, Instance instance);

    /**
     * Revert the host state according to the schedule result.
     * If the IntraScheduler synchronizes a certain partition during the period InnerScheduleBegin~InnerScheduleEnd,
     * it will clear all the selfHostStates in this area, and the scheduler actually thinks that it has successfully scheduled itself,
     * so we need to use the IntraScheduler's The scheduling result restores selfHostState.
     * @param intraSchedulerResult the intra-scheduler result.
     * @return the StatesManager itself.
     */
    StatesManager revertHostState(IntraSchedulerResult intraSchedulerResult);

    /**
     * Revert the host state recorded for intra-scheduler independently.
     * After the intra-scheduler allocates an instance to a host,
     * it will update the state of the host to the state that the instance is allocated successfully.
     * But when some instances are allocated failed later,
     * we need to revert the host state to the state before the instance is allocated.
     *
     * @param instances      the instances to be reverted.
     * @param intraScheduler the intra-scheduler.
     * @return the StatesManager itself.
     */
    StatesManager revertSelfHostState(List<Instance> instances, IntraScheduler intraScheduler);

    /**
     * Is the heartbeat needed.
     * If not, it means that the actual host status is always the same as the center host status.
     *
     * @return whether the heartbeat is needed.
     */
    boolean isNeedHeartbeat();

    /**
     * Get the next heartbeat time delay of the host.
     *
     * @param hostId      the id of the host.
     * @param currentTime the current time.
     * @return the next heartbeat time of the host.
     */
    double getNextHeartbeatDelay(int hostId, double currentTime);

    /**
     * In order to save space, heartbeat are only made when the host status changes.
     * We need to synchronize the status of these changed hosts from actualHostStates to centerHostStates
     *
     * @param updatedHostIds the ids of the hosts that have changed and make heartbeat
     * @return the StatesManager itself.
     */
    StatesManager synByHeartbeat(List<Integer> updatedHostIds);

    /**
     * get the synchronization state of the intra-scheduler.
     *
     * @param intraScheduler the intra-scheduler.
     * @return the synchronization state of the intra-scheduler, see {@link SynState}.
     */
    SynState getSynStateForIntraScheduler(IntraScheduler intraScheduler);

    /**
     * Synchronize all state to the intra-scheduler.
     *
     * @return the StatesManager itself.
     */
    StatesManager synAllStateBetweenCenterAndIntraScheduler();

    /**
     * Get the {@link SimpleState}.
     *
     * @return the simple state.
     */
    SimpleState getSimpleState();

    /**
     * Get the simple state object by type.
     *
     * @param type the type of the simple state.
     * @return the simple state object.
     */
    Object getStateByType(String type);

    /**
     * Whether the synchronization takes time in the data center for the intra-scheduler.
     * If it doesn't take time, it means that the scheduler is state-aware in real time.
     *
     * @return whether the synchronization takes time.
     */
    boolean isSynCostTime();

    /**
     * Get the next partition synchronization delay in the data center for the intra-scheduler.
     *
     * @return the next partition synchronization delay.
     */
    double getNextPartitionSynDelay();

    /**
     * Get how many partition synchronization gaps have been completed.
     *
     * @return how many partition synchronization gaps have been completed.
     */
    int getPartitionSynCount();

    /**
     * Whether the time is in the latest partition synchronization gap.
     *
     * @param time the time.
     * @return whether the time is in the latest partition synchronization gap.
     */
    boolean isInLatestPartitionSynGap(double time);

    /**
     * Get the host capacity according to the host id.
     *
     * @param hostId the host id.
     * @return the host capacity.
     */
    int[] getHostCapacity(int hostId);

    /**
     * Get the host capacity manager.
     *
     * @return the host capacity manager.
     */
    HostCapacityManager getHostCapacityManager();

    /**
     * Get the max cpu capacity among all hosts in the datacenter.
     *
     * @return the max cpu capacity among all hosts in the datacenter.
     */
    int getMaxCpuCapacity();

    /**
     * Get the max ram capacity among all hosts in the datacenter.
     *
     * @return the max ram capacity among all hosts in the datacenter.
     */
    int getMaxRamCapacity();

    /**
     * Whether to use the prediction function.
     *
     * @return whether to use the prediction function.
     */
    boolean getPredictable();

    /**
     * Set the record data num for predicting.
     * @param predictRecordNum the record data num for predicting.
     * @return the StatesManager itself.
     */
    StatesManager setPredictRecordNum(int predictRecordNum);

    /**
     * Get the record data num for predicting.
     * @return the record data num for predicting.
     */
    int getPredictRecordNum();

    /**
     * Set whether to use the prediction function.
     * @param predictable whether to use the prediction function.
     * @return the StatesManager itself.
     */
    StatesManager setPredictable(boolean predictable);

    /**
     * Get the {@link PredictionManager}.
     * @return the prediction manager.
     */
    PredictionManager getPredictionManager();

    /**
     * Set the {@link PredictionManager}.
     * @param predictionManager the prediction manager.
     * @return the StatesManager itself.
     */
    StatesManager setPredictionManager(PredictionManager predictionManager);

    /**
     * Adjust the schedule view of every intra-scheduler.
     * The default schedule view is the entire datacenter.
     * @return the StatesManager itself.
     */
    StatesManager adjustScheduleView();

    /**
     * Get the intra-scheduler view that the intra-scheduler can schedule the instances to.
     * @param intraScheduler the intra-scheduler.
     * @return the intra-scheduler view.
     */
    List<Integer> getIntraSchedulerView(IntraScheduler intraScheduler);

    /**
     * Get the {@link DatacenterPowerOnRecord}.
     * @return the datacenter power on record.
     */
    DatacenterPowerOnRecord getDatacenterPowerOnRecord();

    /**
     * Get the total CPU capacity of the datacenter.
     * @return the total CPU capacity of the datacenter.
     */
    long getTotalCPU();

    /**
     * Get the total RAM capacity of the datacenter.
     * @return the total RAM capacity of the datacenter.
     */
    long getTotalRAM();

    /**
     * Get the total storage capacity of the datacenter.
     * @return the total storage capacity of the datacenter.
     */
    long getTotalStorage();

    /**
     * Get the total bandwidth capacity of the datacenter.
     * @return the total bandwidth capacity of the datacenter.
     */
    long getTotalBw();
}
