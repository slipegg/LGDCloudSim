package org.scalecloudsim.datacenters;

import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.Instances.InstanceGroup;

import java.util.List;

public interface InstanceQueue {
    int getInstanceNum();

    List<Instance> getBatchItem();

    InstanceQueue add(Instance instance);

    InstanceQueue add(InstanceGroup instanceGroup);
}
