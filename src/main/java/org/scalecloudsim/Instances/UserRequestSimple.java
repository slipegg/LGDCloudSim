package org.scalecloudsim.Instances;

import java.util.List;

public class UserRequestSimple implements UserRequest {
    int id;
    double submitTime;
    List<InstanceGroup> instanceGroups;
    InstanceGroupGraph instanceGroupGraph;

    public UserRequestSimple(int id, List<InstanceGroup> instanceGroups, InstanceGroupGraph instanceGroupGraph) {
        this.id = id;
        this.instanceGroups = instanceGroups;
        this.instanceGroupGraph = instanceGroupGraph;
    }

    public UserRequestSimple(int id) {
        this.id = id;
    }

    public UserRequestSimple() {
        this.id = -1;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public List<InstanceGroup> getInstanceGroups() {
        return instanceGroups;
    }

    @Override
    public UserRequest setInstanceGroups(List<InstanceGroup> instanceGroups) {
        this.instanceGroups = instanceGroups;
        return this;
    }

    @Override
    public InstanceGroupGraph getInstanceGroupGraph() {
        return instanceGroupGraph;
    }

    @Override
    public UserRequest setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph) {
        this.instanceGroupGraph = instanceGroupGraph;
        return this;
    }

    @Override
    public UserRequest setSubmitTime(double submitTime) {
        this.submitTime=submitTime;
        return this;
    }

    @Override
    public double getSubmitTime() {
        return submitTime;
    }
}
