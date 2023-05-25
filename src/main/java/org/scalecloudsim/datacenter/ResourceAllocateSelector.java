package org.scalecloudsim.datacenter;

import org.scalecloudsim.innerscheduler.InnerScheduleResult;
import org.scalecloudsim.request.Instance;

import java.util.List;
import java.util.Map;

public interface ResourceAllocateSelector {
    ResourceAllocateSelector setDatacenter(Datacenter datacenter);

    Datacenter getDatacenter();

    Map<Integer, List<Instance>> selectResourceAllocate(List<InnerScheduleResult> innerScheduleResults);

    Map<Integer, Integer> getPartitionConflicts();
}
