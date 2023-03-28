package org.scalecloudsim.resourcemanager;

import org.cloudsimplus.core.DatacenterEntity;

import java.util.List;
//this is the only interface that can be used by user
public interface StateManager extends DatacenterEntity {
    StateManager setPartitionRanges(List<PartitionRange> ranges);//支持删除原有分区，重新设置分区
    StateManager addPartitionWatch(int rangeId,double delay);
    StateManager delPartitionWatch(int rangeId,double delay);
    StateManager addAllPartitionWatch(double delay);
    PartitionRange getPartitionRangeById(int id);
    List<HostResourceState> getPartitionDelayState(int id, double delay);
}
