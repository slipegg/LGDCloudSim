package org.scalecloudsim.datacenter;

import org.scalecloudsim.request.InstanceGroup;
import org.scalecloudsim.request.UserRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GroupQueueFifo implements GroupQueue {
    private List<InstanceGroup> instanceGroups;

    private int batchNum;

    public GroupQueueFifo() {
        instanceGroups = new LinkedList<>();
        batchNum = 5;
    }

    @Override
    public GroupQueue add(List<UserRequest> userRequests) {
        for (UserRequest userRequest : userRequests) {
            add(userRequest);
        }
        return this;
    }

    @Override
    public GroupQueue add(UserRequest userRequest) {//先到先服务在到来时不需要排队
        List<InstanceGroup> instanceGroups = userRequest.getInstanceGroups();
        this.instanceGroups.addAll(instanceGroups);
        return this;
    }

    @Override
    public GroupQueue add(InstanceGroup instanceGroup) {
        this.instanceGroups.add(instanceGroup);
        return this;
    }

    @Override
    public List<InstanceGroup> getBatchItem() {
        List<InstanceGroup> sendInstanceGroups = new ArrayList<>();
        for (int i = 0; i < batchNum; i++) {
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

}
