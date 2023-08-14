package org.cpnsim.statemanager;

import java.util.List;

public interface PredictionManager {
    int[] predictHostState(List<HostStateHistory> hostStateHistories);
}
