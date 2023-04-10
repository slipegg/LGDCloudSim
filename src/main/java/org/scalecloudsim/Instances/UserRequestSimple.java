package org.scalecloudsim.Instances;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.datacenters.Datacenter;

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

    public UserRequestSimple(int id, List<InstanceGroup> instanceGroups, InstanceGroupGraph instanceGroupGraph) {
        this.id = id;
        setInstanceGroups(instanceGroups);
        this.instanceGroupGraph = instanceGroupGraph;
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
        this.instanceGroupGraph=instanceGroupGraph;
        this.instanceGroupGraph.setUserRequest(this);
        return this;
    }
}
