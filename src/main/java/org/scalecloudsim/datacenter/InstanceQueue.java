package org.scalecloudsim.datacenter;

import org.scalecloudsim.request.Instance;
import org.scalecloudsim.request.InstanceGroup;

import java.util.List;

public interface InstanceQueue {
    int size();

    List<Instance> getBatchItem();

    InstanceQueue add(Instance instance);

    InstanceQueue add(InstanceGroup instanceGroup);

    InstanceQueue add(List<InstanceGroup> instanceGroups);
}
