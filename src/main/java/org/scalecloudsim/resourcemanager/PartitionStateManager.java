package org.scalecloudsim.resourcemanager;

import java.util.List;

public interface PartitionStateManager {
    PartitionStateManager setPartitionRange(PartitionRange partitionRange);//左闭右闭
    PartitionRange getPartitionRange();
    PartitionStateManager addDelayWatch(double delay);
    List<HostResourceState> getPartitionDelayState(double delay);
}
