package org.cpnsim.datacenter;

import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.util.List;

public interface GroupQueue {
    GroupQueue add(List<UserRequest> userRequests);

    GroupQueue add(UserRequest userRequest);

    GroupQueue add(InstanceGroup instanceGroup);

    List<InstanceGroup> getBatchItem();

    List<InstanceGroup> getAllItem();

    int size();
}
