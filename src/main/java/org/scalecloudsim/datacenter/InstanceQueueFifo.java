package org.scalecloudsim.datacenter;

import org.scalecloudsim.request.Instance;
import org.scalecloudsim.request.InstanceGroup;
import org.scalecloudsim.request.UserRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InstanceQueueFifo implements InstanceQueue {
    private List<Instance> instances;
    private int batchNum;

    public InstanceQueueFifo() {
        instances = new LinkedList<>();
        batchNum = 6;
    }

    public InstanceQueueFifo(int batchNum) {
        instances = new LinkedList<>();
        this.batchNum = batchNum;
    }

    @Override
    public int size() {
        return instances.size();
    }

    @Override
    public List<Instance> getBatchItem() {
        List<Instance> sendInstances = new ArrayList<>();
        for (int i = 0; i < batchNum; i++) {
            if (this.instances.size() == 0) {
                break;
            }
            if (this.instances.get(0).getUserRequest().getState() == UserRequest.FAILED) {
                this.instances.remove(0);
                continue;
            }
            sendInstances.add(this.instances.remove(0));
        }
        return sendInstances;
    }

    @Override
    public InstanceQueue add(Instance instance) {
        this.instances.add(instance);
        return this;
    }

    @Override
    public InstanceQueue add(InstanceGroup instanceGroup) {
        this.instances.addAll(instanceGroup.getInstanceList());
        return this;
    }

    @Override
    public InstanceQueue add(List<InstanceGroup> instanceGroups) {
        for (InstanceGroup instanceGroup : instanceGroups) {
            this.instances.addAll(instanceGroup.getInstanceList());
        }
        return this;
    }
}
