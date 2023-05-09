package org.cloudsimplus.core;

import org.scalecloudsim.datacenter.LoadBalance;
import org.scalecloudsim.datacenter.ResourceAllocateSelector;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.interscheduler.InterScheduler;
import org.scalecloudsim.statemanager.PredictionManager;

import java.util.Map;

public interface Factory {
    InnerScheduler getInnerScheduler(String type, int id, int firstPartitionId, int partitionNum);

    PredictionManager getPredictionManager(String type);

    InterScheduler getInterScheduler(String type);

    LoadBalance getLoadBalance(String type);

    ResourceAllocateSelector getResourceAllocateSelector(String type);

}
