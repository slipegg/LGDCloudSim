package org.cpnsim.statemanager;

import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterPowerOnRecord;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.innerscheduler.InnerSchedulerResult;
import org.cpnsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An interface to be implemented by each class that is responsible for managing the states of the datacenter.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface StatesManager {
    /**
     * Logger for this class.
     */
    Logger LOGGER = LoggerFactory.getLogger(StatesManager.class.getSimpleName());

    /** Get the datacenter. */
    Datacenter getDatacenter();

    /** Set the datacenter. */
    StatesManager setDatacenter(Datacenter datacenter);

    /** Init the states of the datacenter. */
    StatesManager initHostStates(int cpu, int ram, int storage, int bw, int startId, int length);

    /** get the state for {@link InnerScheduler} */
    SynState getSynState(InnerScheduler innerScheduler);

    /** Synchronize the states of the datacenter. */
    StatesManager synAllState();

    /** Allocate the instance on the host with hostId. */
    boolean allocate(int hostId, Instance instance);

    /** Release the instance on the host with hostId. */
    StatesManager release(int hostId, Instance instance);

    /**
     * Get the {@link DatacenterPowerOnRecord}.
     */
    DatacenterPowerOnRecord getDatacenterPowerOnRecord();

    /**
     * Get the {@link PartitionRangesManager}.
     */
    PartitionRangesManager getPartitionRangesManager();

    /**
     * Get the {@link SimpleState}.
     */
    SimpleState getSimpleState();

    Object getStateByType(String type);

    /**
     * Whether to use the prediction function.
     */
    boolean getPredictable();

    /**
     * Set whether to use the prediction function.
     */
    StatesManager setPredictable(boolean predictable);

    /**
     * Get the {@link PredictionManager}.
     */
    PredictionManager getPredictionManager();

    /** Set the {@link PredictionManager}. */
    StatesManager setPredictionManager(PredictionManager predictionManager);

    /**
     * Initialization the host state with {@link HostStateGenerator}.
     */
    StatesManager initHostStates(HostStateGenerator hostStateGenerator);

    /**
     * Get the host status at the current moment according to the host id.
     */
    HostState getNowHostState(int hostId);

    /**
     * Get the host num in all datacenter.
     */
    int getHostNum();

    /**
     * Revert the host state according to the schedule result.
     * If the InnerScheduler synchronizes a certain partition during the period InnerScheduleBegin~InnerScheduleEnd,
     * it will clear all the selfHostStates in this area, and the scheduler actually thinks that it has successfully scheduled itself,
     * so we need to use the InnerScheduler's The scheduling result restores selfHostState.
     */
    StatesManager revertHostState(InnerSchedulerResult innerSchedulerResult);

    StatesManager revertSelftHostState(List<Instance> instances, InnerScheduler innerScheduler);

    /**
     * Set the record data num for predicting.
     */
    StatesManager setPredictRecordNum(int predictRecordNum);

    /**
     * Get the record data num for predicting.
     */
    int getPredictRecordNum();

    int getTotalCpuInUse();

    int getTotalRamInUse();

    int getTotalStorageInUse();

    int getTotalBwInUse();

    int getMaxCpuCapacity();

    int getMaxRamCapacity();

    boolean isSynCostTime();

    double getNextSynDelay();

    int getSmallSynGapCount();

    boolean isInLatestSmallSynGap(double time);

    boolean allocate(Instance instance);

    int[] getHostCapacity(int hostId);

    HostCapacityManager getHostCapacityManager();

    StatesManager adjustScheduleView();

    List<Integer> getInnerSchedulerView(InnerScheduler innerScheduler);
}
