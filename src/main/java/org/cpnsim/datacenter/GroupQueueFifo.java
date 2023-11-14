package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A class to represent a instanceGroup queue with first in first out.
 * This class implements the interface {@link GroupQueue}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class GroupQueueFifo implements GroupQueue {
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

    public GroupQueueFifo() {
        instanceGroups = new LinkedList<>();
        batchNum = 2000;
    }

    @Override
    public GroupQueue add(List<?> userRequestsOrInstanceGroups) {
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
    public GroupQueue add(UserRequest userRequest) {//先到先服务在到来时不需要排队
        if (userRequest.getState() == UserRequest.FAILED) {
            return this;
        }

        List<InstanceGroup> instanceGroups = userRequest.getInstanceGroups();
        this.instanceGroups.addAll(instanceGroups);
        return this;
    }

    @Override
    public GroupQueue add(InstanceGroup instanceGroup) {
        if (instanceGroup.getUserRequest().getState() == UserRequest.FAILED) {
            return this;
        }

        this.instanceGroups.add(instanceGroup);
        return this;
    }

    @Override
    public List<InstanceGroup> getBatchItem() {
        return getItems(batchNum);
    }

    @Override
    public List<InstanceGroup> getAllItem() {
        return getItems(this.instanceGroups.size());
    }

    private List<InstanceGroup> getItems(int num) {
        List<InstanceGroup> sendInstanceGroups = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            if (instanceGroups.size() == 0) {
                break;
            }
            if (instanceGroups.get(0).getUserRequest().getState() == UserRequest.FAILED) {
                instanceGroups.remove(0);
                continue;
            }
            sendInstanceGroups.add(instanceGroups.remove(0));
        }
        return sendInstanceGroups;
    }

    @Override
    public int size() {
        return instanceGroups.size();
    }

    @Override
    public boolean isEmpty() {
        return instanceGroups.size() == 0;
    }

}
