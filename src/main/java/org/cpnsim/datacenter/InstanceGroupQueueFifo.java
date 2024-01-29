package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.util.*;

/**
 * A class to represent a instanceGroup queue with first in first out.
 * This class implements the interface {@link InstanceGroupQueue}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class InstanceGroupQueueFifo implements InstanceGroupQueue {
    /**
     * the list of instanceGroups in the queue.
     **/
    private List<InstanceGroup> instanceGroups;

    /**
     * the number of instanceGroups to be sent in a batch.
     **/
    @Getter
    @Setter
    private int batchNum;

    public InstanceGroupQueueFifo() {
        this.instanceGroups = new LinkedList<>();
        this.batchNum = 10000;
    }

    @Override
    public InstanceGroupQueue add(List<?> userRequestsOrInstanceGroups) {
        if (userRequestsOrInstanceGroups.size() != 0) {
            if (userRequestsOrInstanceGroups.get(0) instanceof UserRequest) {
                for (UserRequest userRequest : (List<UserRequest>) userRequestsOrInstanceGroups) {
                    add(userRequest);
                }
            } else if (userRequestsOrInstanceGroups.get(0) instanceof InstanceGroup) {
                for (InstanceGroup instanceGroup : (List<InstanceGroup>) userRequestsOrInstanceGroups) {
                    add(instanceGroup);
                }
            } else {
                throw new RuntimeException("The type of the list is not supported.");
            }
        }
        return this;
    }

    @Override
    public InstanceGroupQueue add(UserRequest userRequest) {//先到先服务在到来时不需要排队
        if (userRequest.getState() == UserRequest.FAILED) {
            return this;
        }

        List<InstanceGroup> instanceGroups = userRequest.getInstanceGroups();
        this.instanceGroups.addAll(instanceGroups);
        return this;
    }

    @Override
    public InstanceGroupQueue add(InstanceGroup instanceGroup) {
        if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
            return this;
        }

        this.instanceGroups.add(instanceGroup);
        return this;
    }

    @Override
    public QueueResult<InstanceGroup> getBatchItem(double nowTime) {
        return getItems(batchNum, nowTime);
    }

    @Override
    public QueueResult<InstanceGroup> getItems(int num, double nowTime) {
        List<InstanceGroup> sendInstanceGroups = new ArrayList<>();
        Set<UserRequest> failedUserRequests = new HashSet<>();

        for (int i = 0; i < num; i++) {
            if (instanceGroups.isEmpty()) {
                break;
            }
            UserRequest userRequest = instanceGroups.get(0).getUserRequest();
            if (userRequest.getState() == UserRequest.FAILED) {
                instanceGroups.remove(0);
                continue;
            }
            if (userRequest.getScheduleDelayLimit() > 0 && nowTime - userRequest.getSubmitTime() > userRequest.getScheduleDelayLimit()) {
                failedUserRequests.add(userRequest);
                instanceGroups.remove(0);
                continue;
            }
            sendInstanceGroups.add(instanceGroups.remove(0));
        }
        return new QueueResult(sendInstanceGroups, failedUserRequests);
    }

    @Override
    public int size() {
        return instanceGroups.size();
    }

    @Override
    public boolean isEmpty() {
        return instanceGroups.isEmpty();
    }

}
