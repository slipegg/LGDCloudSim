package org.scalecloudsim.datacenters;

import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.Instances.InstanceGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InstanceQueueFifo implements InstanceQueue {
    private List<Instance> instances;
    private int batchNum;

    public InstanceQueueFifo() {
        instances = new LinkedList<>();
        batchNum = 5;
    }

    @Override
    public int getInstanceNum() {
        return instances.size();
    }

    @Override
    public List<Instance> getBatchItem() {
        List<Instance> instances = new ArrayList<>();
        for (int i = 0; i < batchNum; i++) {
            if (this.instances.size() == 0) {
                break;
            }
            instances.add(this.instances.remove(0));
        }
        return instances;
    }

    @Override
    public InstanceQueue addAInstance(Instance instance) {
        this.instances.add(instance);
        return this;
    }

    @Override
    public InstanceQueue addInstances(InstanceGroup instanceGroup) {
        this.instances.addAll(instanceGroup.getInstanceList());
        return this;
    }
}
