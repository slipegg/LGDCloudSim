package org.cloudsimplus.core;

import org.cpnsim.datacenter.LoadBalance;
import org.cpnsim.datacenter.ResourceAllocateSelector;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.statemanager.PredictionManager;

public interface Factory {
    InnerScheduler getInnerScheduler(String type, int id, int firstPartitionId, int partitionNum);

    PredictionManager getPredictionManager(String type);

    InterScheduler getInterScheduler(String type);

    LoadBalance getLoadBalance(String type);

    ResourceAllocateSelector getResourceAllocateSelector(String type);
}
