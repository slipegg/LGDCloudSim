package org.scalecloudsim.datacenters;

import org.scalecloudsim.Instances.InstanceGroup;
import org.scalecloudsim.Instances.UserRequest;

import java.util.List;

public interface GroupQueue {
    GroupQueue add(List<UserRequest> userRequests);

    GroupQueue add(UserRequest userRequest);

    GroupQueue add(InstanceGroup instanceGroup);

    List<InstanceGroup> getBatchItem();

    int size();
}
