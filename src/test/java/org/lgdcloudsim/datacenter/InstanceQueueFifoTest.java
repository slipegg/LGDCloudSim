package org.lgdcloudsim.datacenter;

import org.lgdcloudsim.queue.InstanceQueueFifo;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceSimple;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.request.UserRequestSimple;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class InstanceQueueFifoTest {
    @Test
    void testInstanceQueueFifo() {
        UserRequest userRequest = new UserRequestSimple(0);

        Instance instance0 = new InstanceSimple(0, 1, 1, 1, 1);
        instance0.setUserRequest(userRequest);
        Instance instance1 = new InstanceSimple(1, 1, 1, 1, 1);
        instance1.setUserRequest(userRequest);
        Instance instance2 = new InstanceSimple(2, 1, 1, 1, 1);
        instance2.setUserRequest(userRequest);
        Instance instance3 = new InstanceSimple(3, 1, 1, 1, 1);
        instance3.setUserRequest(userRequest);
        Instance instance4 = new InstanceSimple(4, 1, 1, 1, 1);
        instance4.setUserRequest(userRequest);

        InstanceQueueFifo instanceQueueFifoTest = new InstanceQueueFifo();
        instanceQueueFifoTest.add(instance0);
        instanceQueueFifoTest.add(instance1);
        instanceQueueFifoTest.add(instance2);
        instanceQueueFifoTest.add(instance3);
        instanceQueueFifoTest.add(instance4);

        assertEquals(5, instanceQueueFifoTest.size());
        instanceQueueFifoTest.setBatchNum(2);
        assertEquals(2, instanceQueueFifoTest.getBatchItem(-1).getWaitScheduledItems().size());
        assertEquals(3, instanceQueueFifoTest.getAllItem().size());
    }
}
