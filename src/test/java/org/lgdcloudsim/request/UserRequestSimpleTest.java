//package org.lgdcloudsim.request;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class UserRequestSimpleTest {
//
//    @Test
//    public void testAddSuccessGroupNum() {
//        // create an UserRequestSimple object for testing
//        UserRequest ur = new UserRequestSimple(0);
//        // create an InstanceGroupSimple with 2 InstanceGroup
//        InstanceGroup ig1 = new InstanceGroupSimple(1);
//        InstanceGroup ig2 = new InstanceGroupSimple(2);
//        List<InstanceGroup> l = Arrays.asList(ig1, ig2);
//        // add the InstanceGroup to the UserRequestSimple object
//        ur.setInstanceGroups(l);
//        // expect: ur.state == UserRequest.WAITING
//        assertEquals(UserRequest.WAITING, ur.getState());
//        // increment ur's successGroupNum, from 0 to 1
//        ur.addSuccessGroupNum();
//        // expect: ur.state == UserRequest.WAITING
//        assertEquals(UserRequest.WAITING, ur.getState());
//        // increment ur's successGroupNum, from 1 to 2
//        ur.addSuccessGroupNum();
//        // expect: ur.state == UserRequest.SUCCESS
//        assertEquals(UserRequest.SUCCESS, ur.getState());
//    }
//
//}

package org.lgdcloudsim.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserRequestSimpleTest {

    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequestSimple(1);
    }

    @Test
    void testConstructor() {
        assertNotNull(userRequest);
        assertEquals(1, userRequest.getId());
        assertEquals(UserRequest.WAITING, userRequest.getState());
        assertEquals("", userRequest.getFailReason());
        assertNotNull(userRequest.getAllocatedEdges());
        assertTrue(userRequest.getAllocatedEdges().isEmpty());
        assertEquals(-1, userRequest.getScheduleDelayLimit());
    }

    @Test
    void testSetInstanceGroups() {
        List<InstanceGroup> instanceGroups = createDummyInstanceGroups(3);
        userRequest.setInstanceGroups(instanceGroups);
        assertEquals(instanceGroups, userRequest.getInstanceGroups());
        for (InstanceGroup instanceGroup : instanceGroups) {
            assertEquals(userRequest, instanceGroup.getUserRequest());
            for (Instance instance : instanceGroup.getInstances()) {
                assertEquals(instanceGroup, instance.getInstanceGroup());
            }
        }
    }

    @Test
    void testSetInstanceGroupGraph() {
        InstanceGroupGraph instanceGroupGraph = new InstanceGroupGraphSimple(true);
        userRequest.setInstanceGroupGraph(instanceGroupGraph);
        assertEquals(instanceGroupGraph, userRequest.getInstanceGroupGraph());
        assertEquals(userRequest, instanceGroupGraph.getUserRequest());
    }

    @Test
    void testAddSuccessGroupNum() {
        List<InstanceGroup> instanceGroups = createDummyInstanceGroups(3);
        userRequest.setInstanceGroups(instanceGroups);
        userRequest.addSuccessGroupNum();
        userRequest.addSuccessGroupNum();
        assertEquals(UserRequest.WAITING, userRequest.getState());
        userRequest.addSuccessGroupNum();
        assertEquals(UserRequest.SUCCESS, userRequest.getState());
    }

    @Test
    void testAddAllocatedEdge() {
        InstanceGroupEdge edge = new InstanceGroupEdgeSimple(new InstanceGroupSimple(1), new InstanceGroupSimple(2), 10.0, 100.0);
        userRequest.addAllocatedEdge(edge);
        assertFalse(userRequest.getAllocatedEdges().isEmpty());
        assertTrue(userRequest.getAllocatedEdges().contains(edge));
    }

    @Test
    void testDelAllocatedEdge() {
        InstanceGroupEdge edge = new InstanceGroupEdgeSimple(new InstanceGroupSimple(1), new InstanceGroupSimple(2), 10.0, 100.0);
        userRequest.addAllocatedEdge(edge);
        assertFalse(userRequest.getAllocatedEdges().isEmpty());
        userRequest.delAllocatedEdge(edge);
        assertTrue(userRequest.getAllocatedEdges().isEmpty());
    }

    @Test
    void testAddFailReason() {
        assertEquals("", userRequest.getFailReason());
        userRequest.addFailReason("Reason1");
        assertEquals("Reason1", userRequest.getFailReason());
        userRequest.addFailReason("Reason2");
        assertEquals("Reason1-Reason2", userRequest.getFailReason());
    }

    @Test
    public void testUserRequest() {
        UserRequest userRequest = new UserRequestSimple(0);
        List<InstanceGroup> instanceGroups = new ArrayList<>();

        int instanceId = 0;
        int instanceGroupId = 0;
        for (int j = 0; j < 3; j++) {
            InstanceGroup instanceGroup = new InstanceGroupSimple(instanceGroupId++);
            List<Instance> instances = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Instance instance = new InstanceSimple(instanceId++, i, i, i, i);
                instances.add(instance);
            }
            instanceGroup.setInstances(instances);
            instanceGroups.add(instanceGroup);
        }

        InstanceGroupGraph instanceGroupGraph = new InstanceGroupGraphSimple(false);
        InstanceGroupEdge instanceGroupEdge = new InstanceGroupEdgeSimple(instanceGroups.get(0), instanceGroups.get(1), 1, 20);
        instanceGroupGraph.addEdge(instanceGroupEdge);
        instanceGroupEdge = new InstanceGroupEdgeSimple(instanceGroups.get(1), instanceGroups.get(2), 2, 30);
        instanceGroupGraph.addEdge(instanceGroupEdge);

        userRequest.setInstanceGroups(instanceGroups);
        userRequest.setInstanceGroupGraph(instanceGroupGraph);
        userRequest.setSubmitTime(1.0);
        userRequest.setBelongDatacenterId(1);

        int expectedSum = 1 + 2 + 3;
        assertEquals(expectedSum, userRequest.getInstanceGroups().get(0).getCpuSum());
        assertEquals(expectedSum, userRequest.getInstanceGroups().get(0).getRamSum());
        assertEquals(expectedSum, userRequest.getInstanceGroups().get(0).getStorageSum());
        assertEquals(expectedSum, userRequest.getInstanceGroups().get(0).getBwSum());

        int expectedLifeTime = -1;
        assertEquals(expectedLifeTime, userRequest.getInstanceGroups().get(0).getInstances().get(0).getLifecycle());
        int expectedCpu = 1;
        assertEquals(expectedCpu, userRequest.getInstanceGroups().get(0).getInstances().get(1).getCpu());
        int expectedRam = 1;
        assertEquals(expectedRam, userRequest.getInstanceGroups().get(0).getInstances().get(1).getRam());
        int expectedStorage = 1;
        assertEquals(expectedStorage, userRequest.getInstanceGroups().get(0).getInstances().get(1).getStorage());
        int expectedBandwidth = 1;
        assertEquals(expectedBandwidth, userRequest.getInstanceGroups().get(0).getInstances().get(1).getBw());

        double expectDelay = 2;
        assertEquals(expectDelay, userRequest.getInstanceGroupGraph().getEdge(instanceGroups.get(1), instanceGroups.get(2)).getMaxDelay());
        assertEquals(expectDelay, userRequest.getInstanceGroupGraph().getEdge(instanceGroups.get(2), instanceGroups.get(1)).getMaxDelay());

        int expectedRequiredBw = 20;
        assertEquals(expectedRequiredBw, userRequest.getInstanceGroupGraph().getEdge(instanceGroups.get(0), instanceGroups.get(1)).getRequiredBw());
        assertEquals(expectedRequiredBw, userRequest.getInstanceGroupGraph().getEdge(instanceGroups.get(1), instanceGroups.get(0)).getRequiredBw());

        double expectedSubmitTime = 1.0;
        assertEquals(expectedSubmitTime, userRequest.getSubmitTime());

        int expectedBelongDatacenterId = 1;
        assertEquals(expectedBelongDatacenterId, userRequest.getBelongDatacenterId());

    }

    private List<InstanceGroup> createDummyInstanceGroups(int count) {
        List<InstanceGroup> instanceGroups = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            instanceGroups.add(new InstanceGroupSimple(i));
        }
        return instanceGroups;
    }
}

