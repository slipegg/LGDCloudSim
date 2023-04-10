package org.scalecloudsim.datacenters;

import org.scalecloudsim.Instances.Instance;

import java.util.List;
import java.util.Map;

public interface ResourceAllocateSelector {
    ResourceAllocateSelector setDatacenter(Datacenter datacenter);

    Datacenter getDatacenter();

    Map<Integer, List<Instance>> selectResourceAllocate(Map<Integer, List<Instance>> schedule);
}
