package org.cpnsim.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserRequestSimple implements UserRequest {
    @Getter
    private int id;
    private double submitTime;
    private double finishTime;
    private int belongDatacenterId;
    private int state;
    @Getter
    private String failReason;
    @Getter
    private List<InstanceGroupEdge> allocatedEdges;
    private int successGroupNum;
    @Getter
    private List<InstanceGroup> instanceGroups;
    @Getter
    private InstanceGroupGraph instanceGroupGraph;

    public UserRequestSimple(int id) {
        this.id = id;
        this.state = UserRequest.WAITING;
        this.finishTime = -1;
        this.failReason = "";
        this.successGroupNum = 0;
        this.allocatedEdges = new ArrayList<>();
    }

    public UserRequestSimple(int id, List<InstanceGroup> instanceGroups, InstanceGroupGraph instanceGroupGraph) {
        this(id);
        setInstanceGroups(instanceGroups);
        this.instanceGroupGraph = instanceGroupGraph;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public UserRequest setInstanceGroups(List<InstanceGroup> instanceGroups) {
        this.instanceGroups=instanceGroups;
        for(InstanceGroup instanceGroup:instanceGroups){
            instanceGroup.setUserRequest(this);
            for (Instance instance : instanceGroup.getInstances()) {
                instance.setInstanceGroup(instanceGroup);
            }
        }
        return this;
    }

    @Override
    public UserRequest setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph) {
        this.instanceGroupGraph = instanceGroupGraph;
        this.instanceGroupGraph.setUserRequest(this);
        return this;
    }

    @Override
    public UserRequest addSuccessGroupNum() {
        successGroupNum++;
        if (successGroupNum == instanceGroups.size()) {
            state = UserRequest.SUCCESS;
        }
        return this;
    }

    @Override
    public UserRequest addAllocatedEdge(InstanceGroupEdge edge) {
        allocatedEdges.add(edge);
        return this;
    }

    @Override
    public UserRequest addFailReason(String failReason) {
        this.failReason = this.failReason + "-" + failReason;
        return this;
    }

    @Override
    public String toString() {
        return "UserRequestSimple [id=" + id + ", submitTime=" + submitTime + ", instanceGroups=" + instanceGroups
                + ", instanceGroupGraph=" + instanceGroupGraph + "]";
    }
}
