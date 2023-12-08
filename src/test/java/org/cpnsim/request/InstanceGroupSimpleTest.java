package org.cpnsim.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

public class InstanceGroupSimpleTest {
    @Test
    public void testAddRetryNum() {
        // create a InstanceGroupSimple object for testing
        InstanceGroupSimple instanceGroupSimple = new InstanceGroupSimple(0);
        // set its retryMaxNum by 1
        instanceGroupSimple.setRetryMaxNum(1);
        // expect: instanceGroupSimple.getState() == UserRequest.WAITING
        assertEquals(UserRequest.WAITING, instanceGroupSimple.getState());

        // add retryNum, so retryNum becomes 1 == retryMaxNum
        // expect: instanceGroupSimple.getState() == UserRequest.FAILED
        instanceGroupSimple.addRetryNum();
        assertEquals(UserRequest.FAILED, instanceGroupSimple.getState());
    }

    @Test
    public void testGetStorageBwCpuRamSum() {
        // create a InstanceGroupSimple object for testing
        InstanceGroupSimple instanceGroupSimple = new InstanceGroupSimple(0);
        // expect: cpu == ram == storage == bw == 0
        assertEquals(0, instanceGroupSimple.getStorageSum());
        assertEquals(0, instanceGroupSimple.getBwSum());
        assertEquals(0, instanceGroupSimple.getCpuSum());
        assertEquals(0, instanceGroupSimple.getRamSum());

        // create a List of Instance object
        List<Instance> instanceSimpleList1 = Arrays.asList(new InstanceSimple(1, 1, 2, 3, 4),
                new InstanceSimple(2, 1, 2, 3, 4), new InstanceSimple(3, 1, 2, 3, 4));
        // set this list of Instance to InstanceGroupSimple
        instanceGroupSimple.setInstances(instanceSimpleList1);
        // expect: cpu == 3, ram == 6, storage == 9, bw == 12
        assertEquals(3, instanceGroupSimple.getCpuSum());
        assertEquals(6, instanceGroupSimple.getRamSum());
        assertEquals(9, instanceGroupSimple.getStorageSum());
        assertEquals(12, instanceGroupSimple.getBwSum());

        // create another List of Instance object
        List<Instance> instanceSimpleList2 = Arrays.asList(new InstanceSimple(1, 5, 6, 7, 8),
                new InstanceSimple(2, 5, 6, 7, 8), new InstanceSimple(3, 5, 6, 7, 8));
        // set this list of Instance to InstanceGroupSimple again
        instanceGroupSimple.setInstances(instanceSimpleList2);
        // expect: cpu == 15, ram == 18, storage == 21, bw == 24
        assertEquals(15, instanceGroupSimple.getCpuSum());
        assertEquals(18, instanceGroupSimple.getRamSum());
        assertEquals(21, instanceGroupSimple.getStorageSum());
        assertEquals(24, instanceGroupSimple.getBwSum());

    }

}
