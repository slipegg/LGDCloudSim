package org.scalecloudsim.statemanager;

import java.util.Map;

public interface PartitionManager {
    PartitionManager addDelayWatch(double delay);

    PartitionManager delDelayWatch(double delay);

    PartitionManager delAllDelayWatch();

    PartitionManager addHostHistory(int hostId, HostStateHistory hostStateHistory);

    PartitionManager updateHostHistory(double lastTime, int hostId);

    Map<Integer, HostStateHistory> getDelayPartitionState(double delay);
}
