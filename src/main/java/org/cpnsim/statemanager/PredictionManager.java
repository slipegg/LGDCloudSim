package org.cpnsim.statemanager;

import java.util.List;

public interface PredictionManager {
    //    Map<Integer, HostStateHistory> predictHostStates(PartitionManager partitionManager, double delay);
    int[] predictHostState(List<HostStateHistory> hostStateHistories);
}
