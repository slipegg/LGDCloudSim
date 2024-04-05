package org.lgdcloudsim.queue;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

import java.util.*;

/**
 * A class to represent a instance queue with first in first out.
 * This class implements the interface {@link InstanceQueue}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
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

    /**
     * the flag to set whether to check if the instance exceeds the maximum schedule delay limit.
     */
    @Getter
    @Setter
    boolean checkOutdatedFlag = false;

    /**
     * Create a new instance of InstanceQueueFifo with the batch number.
     */
    public InstanceQueueFifo(int batchNum) {
        instances = new LinkedList<>();
        this.batchNum = batchNum;
    }

    /**
     * Create a new instance of InstanceQueueFifo with the default batch number.
     */
    public InstanceQueueFifo() {
        this(1000);
    }


    @Override
    public int size() {
        return instances.size();
    }

    @Override
    public QueueResult<Instance> getBatchItem(double nowTime) {
        return getItems(batchNum, nowTime);
    }

    @Override
    public List<Instance> getAllItem() {
        // Because there is no failure handler after this function.
        // So we set nowTime = -1, so that there is no instance will be removed because of the delay limit.
        return getItems(this.instances.size(), -1).getWaitScheduledItems();
    }

    /**
     * Select num instances at the head of the queue.
     *
     * @param num     the number of instances to be selected
     * @param nowTime the current time
     * @return the selected instances
     */
    @Override
    public QueueResult<Instance> getItems(int num, double nowTime) {
        List<Instance> sendInstances = new ArrayList<>();
        Set<UserRequest> failedUserRequests = new HashSet<>();

        for (int i = 0; i < num; i++) {
            if (this.instances.isEmpty()) {
                break;
            }

            UserRequest userRequest = this.instances.get(0).getUserRequest();
            if (userRequest.getState() == UserRequest.FAILED) {
                this.instances.remove(0);
                continue;
            }

            if (checkOutdatedFlag && userRequest.getScheduleDelayLimit() > 0 && nowTime - userRequest.getSubmitTime() > userRequest.getScheduleDelayLimit()) {
                failedUserRequests.add(userRequest);
                this.instances.remove(0);
                continue;
            }

            sendInstances.add(this.instances.remove(0));
        }
        return new QueueResult(sendInstances, failedUserRequests);
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
        for (InstanceGroup instanceGroup : userRequest.getInstanceGroups()) {
            add(instanceGroup);
        }
        return this;
    }

    @Override
    public InstanceQueue add(List requests) {
        if (requests.isEmpty()) {
            return this;
        } else if (requests.get(0) instanceof Instance) {
            this.instances.addAll((List<Instance>) requests);
        } else if (requests.get(0) instanceof InstanceGroup) {
            for (InstanceGroup instanceGroup : (List<InstanceGroup>) requests) {
                this.instances.addAll(instanceGroup.getInstances());
            }
        } else if (requests.get(0) instanceof UserRequest) {
            for (UserRequest userRequest : (List<UserRequest>) requests) {
                add(userRequest);
            }
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        return instances.isEmpty();
    }
}
