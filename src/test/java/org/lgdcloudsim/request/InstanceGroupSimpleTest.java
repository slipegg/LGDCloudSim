package org.lgdcloudsim.request;

import org.lgdcloudsim.datacenter.Datacenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InstanceGroupSimpleTest {

    private InstanceGroupSimple instanceGroup;

    @BeforeEach
    void setUp() {
        instanceGroup = new InstanceGroupSimple(1);
    }

    @Test
    void testConstructorWithEmptyInstanceList() {
        assertNotNull(instanceGroup);
        assertEquals(1, instanceGroup.getId());
        assertNotNull(instanceGroup.getInstances());
        assertTrue(instanceGroup.getInstances().isEmpty());
        assertEquals(0, instanceGroup.getRetryNum());
        assertEquals(0, instanceGroup.getRetryMaxNum());
        assertEquals(UserRequest.WAITING, instanceGroup.getState());
        assertEquals(Double.MAX_VALUE, instanceGroup.getAccessLatency());
        assertEquals(Datacenter.NULL, instanceGroup.getReceiveDatacenter());
        assertEquals(-1, instanceGroup.getReceivedTime());
        assertEquals(-1, instanceGroup.getFinishTime());
        assertEquals(0, instanceGroup.getSuccessInstanceNum());
        assertNotNull(instanceGroup.getForwardDatacenterIdsHistory());
        assertTrue(instanceGroup.getForwardDatacenterIdsHistory().isEmpty());
    }

    @Test
    void testConstructorWithInstanceList() {
        List<Instance> instances = new ArrayList<>();
        instances.add(new InstanceSimple(1, 2, 4, 8, 16));
        instanceGroup = new InstanceGroupSimple(1, instances);
        assertNotNull(instanceGroup);
        assertEquals(1, instanceGroup.getId());
        assertNotNull(instanceGroup.getInstances());
        assertFalse(instanceGroup.getInstances().isEmpty());
        assertEquals(1, instanceGroup.getInstances().size());
    }

    @Test
    void testSetInstances() {
        List<Instance> instances = new ArrayList<>();
        instances.add(new InstanceSimple(1, 2, 4, 8, 16));
        instanceGroup.setInstances(instances);
        assertNotNull(instanceGroup.getInstances());
        assertFalse(instanceGroup.getInstances().isEmpty());
        assertEquals(1, instanceGroup.getInstances().size());
    }

    @Test
    void testSetUserRequest() {
        UserRequest userRequest = new UserRequestSimple(1);
        instanceGroup.setUserRequest(userRequest);
        assertEquals(userRequest, instanceGroup.getUserRequest());
        for (Instance instance : instanceGroup.getInstances()) {
            assertEquals(userRequest, instance.getUserRequest());
        }
    }

    @Test
    void testIsSetDestDatacenter() {
        assertFalse(instanceGroup.isSetDestDatacenter());
        instanceGroup.setDestDatacenterId(1);
        assertTrue(instanceGroup.isSetDestDatacenter());
    }

    @Test
    void testAddRetryNum() {
        assertFalse(instanceGroup.isFailed());
        instanceGroup.setRetryMaxNum(2);
        instanceGroup.addRetryNum();
        instanceGroup.addRetryNum();
        assertFalse(instanceGroup.isFailed());
        instanceGroup.addRetryNum();
        assertTrue(instanceGroup.isFailed());
    }

    @Test
    void testAddSuccessInstanceNum() {
        assertEquals(0, instanceGroup.getSuccessInstanceNum());
        instanceGroup.setInstances(createDummyInstances(3));
        instanceGroup.addSuccessInstanceNum();
        assertEquals(1, instanceGroup.getSuccessInstanceNum());
        instanceGroup.addSuccessInstanceNum();
        instanceGroup.addSuccessInstanceNum();
        assertEquals(3, instanceGroup.getSuccessInstanceNum());
    }

    @Test
    void testAddForwardDatacenterIdHistory() {
        assertTrue(instanceGroup.getForwardDatacenterIdsHistory().isEmpty());
        instanceGroup.addForwardDatacenterIdHistory(1);
        instanceGroup.addForwardDatacenterIdHistory(2);
        assertFalse(instanceGroup.getForwardDatacenterIdsHistory().isEmpty());
        assertEquals(2, instanceGroup.getForwardDatacenterIdsHistory().size());
    }

    @Test
    void testIsNetworkLimited() {
        UserRequest userRequest = new UserRequestSimple(1);
        userRequest.setInstanceGroupGraph(new InstanceGroupGraphSimple(false));
        instanceGroup.setUserRequest(userRequest);

        assertFalse(instanceGroup.isNetworkLimited());
        instanceGroup.setAccessLatency(100);
        assertTrue(instanceGroup.isNetworkLimited());
        instanceGroup.getUserRequest().getInstanceGroupGraph().addEdge(instanceGroup, new InstanceGroupSimple(2), 2, 2);
        assertTrue(instanceGroup.isNetworkLimited());
    }

    @Test
    void testSetId() {
        instanceGroup.setId(2);
        assertEquals(2, instanceGroup.getId());
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

    private List<Instance> createDummyInstances(int count) {
        List<Instance> instances = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            instances.add(new InstanceSimple(i, 2, 4, 8, 16));
        }
        return instances;
    }

}

