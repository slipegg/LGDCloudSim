package org.scalecloudsim.datacenters;

import org.scalecloudsim.Instances.InstanceGroup;
import org.scalecloudsim.Instances.UserRequest;

import java.util.List;

public interface GroupQueue {
    GroupQueue addInstanceGroups(List<UserRequest> userRequests);

    GroupQueue addInstanceGroups(UserRequest userRequest);

    List<InstanceGroup> getInstanceGroups();

    int getGroupNum();
}
