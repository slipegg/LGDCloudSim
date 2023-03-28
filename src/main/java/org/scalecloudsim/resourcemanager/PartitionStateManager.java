package org.scalecloudsim.resourcemanager;

import org.cloudsimplus.core.ChangeableId;
import org.cloudsimplus.core.DatacenterEntity;

import java.util.List;

public interface PartitionStateManager {
    PartitionStateManager setPartitionRange(PartitionRange partitionRange);//左闭右闭
    PartitionRange getPartitionRange();
    PartitionStateManager addDelayWatch(double delay);
    PartitionStateManager delDelayWatch(double delay);
    PartitionStateManager delAllDelayWatch();
    List<HostResourceState> getPartitionDelayState(double delay);
}
