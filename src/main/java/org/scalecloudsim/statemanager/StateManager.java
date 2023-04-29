package org.scalecloudsim.statemanager;

import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.datacenter.Datacenter;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public interface StateManager {
    Logger LOGGER = LoggerFactory.getLogger(StateManager.class.getSimpleName());

    Datacenter getDatacenter();

    StateManager setDatacenter(Datacenter datacenter);

    StateManager setPartitionRanges(PartitionRangesManager partitionRangesManager);//支持删除原有分区，重新设置分区

    StateManager registerScheduler(InnerScheduler scheduler);

    StateManager registerSchedulers(List<InnerScheduler> scheduler);

    StateManager cancelScheduler(InnerScheduler scheduler);

    StateManager calcelAllSchedulers();

    DelayState getDelayState(InnerScheduler scheduler);

    Simulation getSimulation();

    TreeMap<Integer, LinkedList<HostStateHistory>> getHostHistoryMaps();

    int[] getnowHostStateArr(int hostId);

    HostStateHistory getnowHostStateHistory(int hostId);

    HostState getnowHostState(int hostId);

    LinkedList<HostStateHistory> getHostHistory(int hostId);

    HostStateHistory getHostStateHistory(int hostId, double time);

    PartitionRangesManager getPartitionRangesManager();

    StateManager updateHostState(int hostId, int[] state);

    StateManager updateHostState(int hostId);

    SimpleState getSimpleState();

    StateManager setSimpleState(SimpleState simpleState);

    int[] getHostStates();

    StateManager initHostStates(HostStateGenerator hostStateGenerator);

    StateManager initHostStates(int cpu, int ram, int storage, int bw, int startId, int length);

    boolean isSuitable(int hostId, Instance instance);

    boolean allocateResource(int hostId, Instance instance);

    StateManager releaseResource(int hostId, Instance instance);

    List<Double> getPartitionWatchDelay(int hostId);

    boolean getPredictable();

    StateManager setPredictable(boolean predictable);

    PredictionManager getPredictionManager();

    StateManager setPredictionManager(PredictionManager predictionManager);
}
