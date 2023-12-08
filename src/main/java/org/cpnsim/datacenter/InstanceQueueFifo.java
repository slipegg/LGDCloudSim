package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.Setter;
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
    @Getter
    @Setter
    private int batchNum;

    public InstanceQueueFifo(int batchNum) {
        instances = new LinkedList<>();
        this.batchNum = batchNum;
    }

    public InstanceQueueFifo() {
        this(1000);
    }


    @Override
    public int size() {
        return instances.size();
    }

    @Override
    public List<Instance> getBatchItem(boolean isRemove) {
        return getItems(batchNum, isRemove);
    }

    @Override
    public List<Instance> getAllItem(boolean isRemove) {
        return getItems(this.instances.size(), isRemove);
    }

    private List<Instance> getItems(int num, boolean isRemove) {
        List<Instance> sendInstances = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            if (this.instances.size() == 0) {
                break;
            }
            if (this.instances.get(0).getUserRequest().getState() == UserRequest.FAILED) {
                if (isRemove) {
                    this.instances.remove(0);
                } else {
                    sendInstances.add(this.instances.get(i));
                }
                continue;
            }
            if (isRemove) {
                sendInstances.add(this.instances.remove(0));
            } else {
                sendInstances.add(this.instances.get(i));
            }
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
        this.instances.addAll(instanceGroup.getInstances());
        return this;
    }


    @Override
    public InstanceQueue add(UserRequest userRequest) {
        for(InstanceGroup instanceGroup : userRequest.getInstanceGroups()){
            add(instanceGroup);
        }
        return this;
    }

    @Override
    public InstanceQueue add(List requests) {
        if (requests.size() == 0) {
            return this;
        } else if (requests.get(0) instanceof Instance) {
            this.instances.addAll((List<Instance>)requests);
        } else if (requests.get(0) instanceof InstanceGroup) {
            for (InstanceGroup instanceGroup : (List<InstanceGroup>)requests) {
                this.instances.addAll(instanceGroup.getInstances());
            }
        } else if(requests.get(0) instanceof UserRequest){
            for (UserRequest userRequest : (List<UserRequest>)requests) {
                add(userRequest);
            }
        }
        return this;
    }
}
