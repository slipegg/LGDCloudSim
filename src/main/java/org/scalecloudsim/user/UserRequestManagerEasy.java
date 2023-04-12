package org.scalecloudsim.user;

import org.scalecloudsim.request.*;

import java.util.List;

public class UserRequestManagerEasy implements UserRequestManager {
    static int userRequestId = 0;
    static int instanceId = 0;
    static int instanceGroupId = 0;

    @Override
    public List<UserRequest> getUserRequestMap(double startTime, double endTime, int datacenterId) {
        Instance instance = new InstanceSimple(instanceId++, 1, 1, 1, 1, 3000);
        InstanceGroup instanceGroup = new InstanceGroupSimple(instanceGroupId++);
        instanceGroup.setInstanceList(List.of(instance));
        InstanceGroupGraph instanceGroupGraph = new InstanceGroupGraphSimple(false);
        UserRequest userRequest = new UserRequestSimple(userRequestId++, List.of(instanceGroup), instanceGroupGraph);
        userRequest.setSubmitTime(startTime);
        userRequest.setBelongDatacenterId(datacenterId);
        return List.of(userRequest);
    }
}