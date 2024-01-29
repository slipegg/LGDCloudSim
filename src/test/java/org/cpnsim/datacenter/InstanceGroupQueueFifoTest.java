package org.cpnsim.datacenter;

import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.InstanceGroupSimple;
import org.cpnsim.request.UserRequest;
import org.cpnsim.request.UserRequestSimple;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class InstanceGroupQueueFifoTest {
    @Test
    public void testGroupQueueFifo() {
        UserRequest userRequest = new UserRequestSimple(0);
        userRequest.setState(UserRequest.WAITING);
        InstanceGroupQueueFifo groupQueueFifoTest = new InstanceGroupQueueFifo();
        InstanceGroup instanceGroup0 = new InstanceGroupSimple(0);
        instanceGroup0.setUserRequest(userRequest);
        InstanceGroup instanceGroup1 = new InstanceGroupSimple(1);
        instanceGroup1.setUserRequest(userRequest);
        InstanceGroup instanceGroup2 = new InstanceGroupSimple(2);
        instanceGroup2.setUserRequest(userRequest);
        InstanceGroup instanceGroup3 = new InstanceGroupSimple(3);
        instanceGroup3.setUserRequest(userRequest);
        InstanceGroup instanceGroup4 = new InstanceGroupSimple(4);
        instanceGroup4.setUserRequest(userRequest);
        groupQueueFifoTest.add(instanceGroup0);
        groupQueueFifoTest.add(instanceGroup1);
        groupQueueFifoTest.add(instanceGroup2);
        groupQueueFifoTest.add(instanceGroup3);
        groupQueueFifoTest.add(instanceGroup4);

        assertEquals(5, groupQueueFifoTest.size());
        groupQueueFifoTest.setBatchNum(2);
        List<InstanceGroup> instanceGroups = groupQueueFifoTest.getBatchItem();
        assertEquals(2, instanceGroups.size());
        List<InstanceGroup> instanceGroups1 = groupQueueFifoTest.getAllItem();
        assertEquals(3, instanceGroups1.size());
    }
}
