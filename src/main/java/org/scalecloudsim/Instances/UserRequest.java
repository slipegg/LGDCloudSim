package org.scalecloudsim.Instances;

import org.cloudsimplus.core.ChangeableId;

import java.util.List;

public interface UserRequest extends ChangeableId {
    List<InstanceGroup> getInstanceGroups();

    UserRequest setInstanceGroups(List<InstanceGroup> instanceGroups);

    InstanceGroupGraph getInstanceGroupGraph();

    UserRequest setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph);
}
