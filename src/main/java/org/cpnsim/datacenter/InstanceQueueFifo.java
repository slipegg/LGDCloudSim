package org.cpnsim.datacenter;

import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A class to represent a instance queue with first in first out.
 * This class implements the interface {@link InstanceQueue}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class InstanceQueueFifo implements InstanceQueue {
    /**
     * the list of instance in the queue.
     **/
    private List<Instance> instances;

    /**
     * the number of instances to be sent in a batch.
     **/
    private int batchNum;

    public InstanceQueueFifo(int batchNum) {
        instances = new LinkedList<>();
        this.batchNum = batchNum;
    }

    public InstanceQueueFifo() {
        this(100000);
    }


    @Override
    public int size() {
        return instances.size();
    }

    @Override
    public List<Instance> getBatchItem() {
        return getItems(batchNum);
    }

    @Override
    public List<Instance> getAllItem() {
        return getItems(this.instances.size());
    }

    private List<Instance> getItems(int num) {
        List<Instance> sendInstances = new ArrayList<>();
        for (int i = 0; i < num; i++) {
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
    public InstanceQueue add(List instances) {
        if (instances.size() == 0) {
            return this;
        } else if (instances.get(0) instanceof Instance) {
            this.instances.addAll(instances);
        } else if (instances.get(0) instanceof InstanceGroup) {
            for (Object instanceGroup : instances) {
                this.instances.addAll(((InstanceGroup) instanceGroup).getInstanceList());
            }
        }
        return this;
    }
}
