package org.scalecloudsim.datacenters;

import org.scalecloudsim.Instances.InstanceGroup;
import org.scalecloudsim.Instances.UserRequest;

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
    public GroupQueue addInstanceGroups(List<UserRequest> userRequests) {
        for (UserRequest userRequest : userRequests) {
            addInstanceGroups(userRequest);
        }
        return this;
    }

    @Override
    public GroupQueue addInstanceGroups(UserRequest userRequest) {//先到先服务在到来时不需要排队
        List<InstanceGroup> instanceGroups = userRequest.getInstanceGroups();
        this.instanceGroups.addAll(instanceGroups);
        return this;
    }

    @Override
    public GroupQueue addAInstanceGroup(InstanceGroup instanceGroup) {
        this.instanceGroups.add(instanceGroup);
        return this;
    }

    @Override
    public List<InstanceGroup> getBatchItem() {
        List<InstanceGroup> userRequests = new ArrayList<>();
        for (int i = 0; i < batchNum; i++) {
            if (instanceGroups.size() == 0) {
                break;
            }
            userRequests.add(instanceGroups.remove(0));
        }
        return userRequests;
    }

    @Override
    public int getGroupNum() {
        return instanceGroups.size();
    }

}
