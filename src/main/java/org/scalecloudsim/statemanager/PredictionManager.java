package org.scalecloudsim.statemanager;

import java.util.Map;

public interface PredictionManager {
    Map<Integer, HostStateHistory> predictHostStates(PartitionManager partitionManager, double delay);
}
