package org.lgdcloudsim.datacenter;

import org.lgdcloudsim.queue.InstanceGroupQueueFifo;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceGroupSimple;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.request.UserRequestSimple;
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
        List<InstanceGroup> instanceGroups = groupQueueFifoTest.getBatchItem(-1).getWaitScheduledItems();
        assertEquals(2, instanceGroups.size());
    }
}
