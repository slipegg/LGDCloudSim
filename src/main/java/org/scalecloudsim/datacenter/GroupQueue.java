package org.scalecloudsim.datacenter;

import org.scalecloudsim.request.InstanceGroup;
import org.scalecloudsim.request.UserRequest;

import java.util.List;

public interface GroupQueue {
    GroupQueue add(List<UserRequest> userRequests);

    GroupQueue add(UserRequest userRequest);

    GroupQueue add(InstanceGroup instanceGroup);

    List<InstanceGroup> getBatchItem();

    int size();
}
