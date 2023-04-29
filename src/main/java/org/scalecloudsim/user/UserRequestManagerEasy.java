package org.scalecloudsim.user;

import org.scalecloudsim.request.*;

import java.util.List;
import java.util.Map;

public class UserRequestManagerEasy implements UserRequestManager {
    static int userRequestId = 0;
    static int instanceId = 0;
    static int instanceGroupId = 0;

    @Override
    public List<UserRequest> getUserRequestMap(double startTime, double endTime, int datacenterId) {
        Instance instance1 = new InstanceSimple(instanceId++, 1, 1, 1, 1, 2000);
        Instance instance2 = new InstanceSimple(instanceId++, 2, 2, 2, 2, 3000);
        Instance instance3 = new InstanceSimple(instanceId++, 3, 3, 3, 3, 4000);
        InstanceGroup instanceGroup1 = new InstanceGroupSimple(instanceGroupId++);
        instanceGroup1.setInstanceList(List.of(instance1));
        InstanceGroup instanceGroup2 = new InstanceGroupSimple(instanceGroupId++);
        instanceGroup2.setInstanceList(List.of(instance2));
        InstanceGroup instanceGroup3 = new InstanceGroupSimple(instanceGroupId++);
        instanceGroup3.setInstanceList(List.of(instance3));
        InstanceGroupGraph instanceGroupGraph = new InstanceGroupGraphSimple(false);
        instanceGroupGraph.addEdge(instanceGroup1, instanceGroup2, 2000, 12);
        instanceGroupGraph.addEdge(instanceGroup3, instanceGroup2, 2000, 32);
        UserRequest userRequest = new UserRequestSimple(userRequestId++, List.of(instanceGroup1, instanceGroup2, instanceGroup3), instanceGroupGraph);
        userRequest.setSubmitTime(startTime);
        userRequest.setBelongDatacenterId(datacenterId);
        return List.of(userRequest);
    }

    @Override
    public Map<Integer, List<UserRequest>> generateOnceUserRequests() {
        return null;
    }

    @Override
    public double getNextSendTime() {
        return 0;
    }
}
