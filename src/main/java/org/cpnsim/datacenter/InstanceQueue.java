package org.cpnsim.datacenter;

import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;

import java.util.List;

public interface InstanceQueue {
    int size();

    List<Instance> getBatchItem();

    List<Instance> getAllItem();

    InstanceQueue add(Instance instance);

    InstanceQueue add(InstanceGroup instanceGroup);

    InstanceQueue add(List instances);
}
