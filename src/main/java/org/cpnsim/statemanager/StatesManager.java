package org.cpnsim.statemanager;

import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.DatacenterPowerOnRecord;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface StatesManager {
    Logger LOGGER = LoggerFactory.getLogger(StatesManager.class.getSimpleName());

    Datacenter getDatacenter();

    StatesManager setDatacenter(Datacenter datacenter);

    StatesManager initHostStates(int cpu, int ram, int storage, int bw, int startId, int length);

    SynState getSynState(InnerScheduler innerScheduler);

    StatesManager synAllState();

    boolean allocate(int hostId, Instance instance);

    StatesManager release(int hostId, Instance instance);

    DatacenterPowerOnRecord getDatacenterPowerOnRecord();

    double getSmallSynGap();

    PartitionRangesManager getPartitionRangesManager();

    SimpleState getSimpleState();

    boolean getPredictable();

    StatesManager setPredictable(boolean predictable);

    PredictionManager getPredictionManager();

    StatesManager setPredictionManager(PredictionManager predictionManager);

    StatesManager initHostStates(HostStateGenerator hostStateGenerator);

    HostState getNowHostState(int hostId);

    int getHostNum();

    StatesManager revertHostState(Map<Integer, List<Instance>> scheduleResult, InnerScheduler innerScheduler);
}
