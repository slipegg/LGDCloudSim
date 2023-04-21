package org.scalecloudsim.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class UserRequestSimple implements UserRequest {
    @Getter
    int id;
    @Getter
    @Setter
    double submitTime;
    @Getter
    List<InstanceGroup> instanceGroups;
    @Getter
    InstanceGroupGraph instanceGroupGraph;
    @Getter
    @Setter
    int belongDatacenterId;

    @Getter
    @Setter
    int state;

    @Getter
    @Setter
    String failReason;

    @Getter
    List<InstanceGroupEdge> allocatedEdges;

    int successGroupNum;

    public UserRequestSimple(int id, List<InstanceGroup> instanceGroups, InstanceGroupGraph instanceGroupGraph) {
        this.id = id;
        this.state = UserRequest.WAITING;
        setInstanceGroups(instanceGroups);
        this.instanceGroupGraph = instanceGroupGraph;
        this.successGroupNum = 0;
        this.allocatedEdges = new ArrayList<>();
    }


    public UserRequestSimple(int id) {
        this.id = id;
    }

    public UserRequestSimple() {
        this.id = -1;
    }

    @Override
    public String toString() {
        return "UserRequestSimple [id=" + id + ", submitTime=" + submitTime + ", instanceGroups=" + instanceGroups
                + ", instanceGroupGraph=" + instanceGroupGraph + "]";
    }

    @Override
    public void setId(int id) {
        this.id=id;
    }

    @Override
    public UserRequest setInstanceGroups(List<InstanceGroup> instanceGroups) {
        this.instanceGroups=instanceGroups;
        for(InstanceGroup instanceGroup:instanceGroups){
            instanceGroup.setUserRequest(this);
            for(Instance instance:instanceGroup.getInstanceList()){
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
}
