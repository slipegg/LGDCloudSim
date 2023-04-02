package org.scalecloudsim.statemanager;

import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.innerscheduler.InnerScheduler;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface StateManager {
    StateManager setPartitionRanges(PartitionRangesManager partitionRangesManager);//支持删除原有分区，重新设置分区
    StateManager registerScheduler(InnerScheduler scheduler);
    StateManager registerSchedulers(List<InnerScheduler> scheduler);
    StateManager cancelScheduler(InnerScheduler scheduler);
    StateManager calcelAllSchedulers();

    DelayState getDelayState(InnerScheduler scheduler);
    Simulation getSimulation();
    TreeMap<Integer,LinkedList<HostStateHistory>> getHostHistoryMaps();
    int[] getnowHostStateArr(int hostId);
    HostStateHistory getnowHostStateHistory(int hostId);
    LinkedList<HostStateHistory> getHostHistory(int hostId);
    HostStateHistory getHostStateHistory(int hostId, double time);
    PartitionRangesManager getPartitionRangesManager();

    StateManager updateHostState(int hostId, int[] state);
    StateManager updateHostState(int hostId);
}
