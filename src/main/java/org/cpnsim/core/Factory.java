package org.cpnsim.core;

import org.cpnsim.datacenter.LoadBalance;
import org.cpnsim.datacenter.ResourceAllocateSelector;
import org.cpnsim.intrascheduler.IntraScheduler;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.statemanager.PredictionManager;

public interface Factory {
    IntraScheduler getIntraScheduler(String type, int id, int firstPartitionId, int partitionNum);

    PredictionManager getPredictionManager(String type);

    InterScheduler getInterScheduler(String type, int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward);

    LoadBalance getLoadBalance(String type);

    ResourceAllocateSelector getResourceAllocateSelector(String type);
}
