package org.scalecloudsim.Instances;

import java.util.List;

public class UserRequestSimple implements UserRequest {
    int id;
    List<InstanceGroup> instanceGroups;
    InstanceGroupGraph instanceGroupGraph;

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
}
