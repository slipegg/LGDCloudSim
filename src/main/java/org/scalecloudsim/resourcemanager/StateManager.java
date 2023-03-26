package org.scalecloudsim.resourcemanager;

import org.cloudsimplus.core.DatacenterEntity;

import java.util.List;
import java.util.Map;

public interface StateManager extends DatacenterEntity {
    StateManager setPartitionRange(List<PartitionRange> ranges);
    StateManager addPartitionWatch(int rangeId,double delay);
    StateManager addAllPartitionWatch(double delay);
    PartitionRange getPartitionRangeById(int id);
    PartitionStateManager getPartitionStateManager(int id);
}
