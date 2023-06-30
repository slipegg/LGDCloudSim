package org.cpnsim.datacenter;

import org.cpnsim.innerscheduler.InnerScheduleResult;
import org.cpnsim.request.Instance;

import java.util.List;
import java.util.Map;

public interface ResourceAllocateSelector {
    ResourceAllocateSelector setDatacenter(Datacenter datacenter);

    Datacenter getDatacenter();

    Map<Integer, List<Instance>> selectResourceAllocate(List<InnerScheduleResult> innerScheduleResults);

    Map<Integer, Integer> getPartitionConflicts();
}
