package org.scalecloudsim.statemanager;

import org.cloudsimplus.core.Simulation;
import org.scalecloudsim.innerscheduler.InnerSchedulerSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public interface StateManager {
    public Logger LOGGER = LoggerFactory.getLogger(StateManager.class.getSimpleName());

    StateManager setPartitionRanges(PartitionRangesManager partitionRangesManager);//支持删除原有分区，重新设置分区

    StateManager registerScheduler(InnerSchedulerSimple scheduler);

    StateManager registerSchedulers(List<InnerSchedulerSimple> scheduler);

    StateManager cancelScheduler(InnerSchedulerSimple scheduler);

    StateManager calcelAllSchedulers();

    DelayState getDelayState(InnerSchedulerSimple scheduler);

    Simulation getSimulation();

    TreeMap<Integer, LinkedList<HostStateHistory>> getHostHistoryMaps();

    int[] getnowHostStateArr(int hostId);

    HostStateHistory getnowHostStateHistory(int hostId);

    LinkedList<HostStateHistory> getHostHistory(int hostId);

    HostStateHistory getHostStateHistory(int hostId, double time);

    PartitionRangesManager getPartitionRangesManager();

    StateManager updateHostState(int hostId, int[] state);

    StateManager updateHostState(int hostId);

    SimpleState getSimpleState();

    StateManager setSimpleState(SimpleState simpleState);

    int[] getHostStates();

    StateManager initHostStates(HostStateGenerator hostStateGenerator);
}
