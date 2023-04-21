package org.scalecloudsim.request;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserRequestSimpleTest {
    @Test
    public void testUserRequest() {
        UserRequest userRequest = new UserRequestSimple();
        List<InstanceGroup> instanceGroups=new ArrayList<>();

        int instanceId = 0;
        int instanceGroupId = 0;
        for (int j = 0; j < 3; j++) {
            InstanceGroup instanceGroup = new InstanceGroupSimple(instanceGroupId++);
            List<Instance> instances = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                Instance instance = new InstanceSimple(instanceId++, i, i, i, i);
                instances.add(instance);
            }
            instanceGroup.setInstanceList(instances);
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
        assertEquals(expectedLifeTime, userRequest.getInstanceGroups().get(0).getInstanceList().get(0).getLifeTime());
        int expectedCpu = 1;
        assertEquals(expectedCpu, userRequest.getInstanceGroups().get(0).getInstanceList().get(1).getCpu());
        int expectedRam = 1;
        assertEquals(expectedRam, userRequest.getInstanceGroups().get(0).getInstanceList().get(1).getRam());
        int expectedStorage = 1;
        assertEquals(expectedStorage, userRequest.getInstanceGroups().get(0).getInstanceList().get(1).getStorage());
        int expectedBandwidth = 1;
        assertEquals(expectedBandwidth,userRequest.getInstanceGroups().get(0).getInstanceList().get(1).getBw());

        double expectDelay=2;
        assertEquals(expectDelay,userRequest.getInstanceGroupGraph().getEdge(instanceGroups.get(1),instanceGroups.get(2)).getMinDelay());
        assertEquals(expectDelay,userRequest.getInstanceGroupGraph().getEdge(instanceGroups.get(2),instanceGroups.get(1)).getMinDelay());

        int expectedRequiredBw=20;
        assertEquals(expectedRequiredBw,userRequest.getInstanceGroupGraph().getEdge(instanceGroups.get(0),instanceGroups.get(1)).getRequiredBw());
        assertEquals(expectedRequiredBw,userRequest.getInstanceGroupGraph().getEdge(instanceGroups.get(1),instanceGroups.get(0)).getRequiredBw());

        double expectedSubmitTime=1.0;
        assertEquals(expectedSubmitTime,userRequest.getSubmitTime());

        int expectedBelongDatacenterId=1;
        assertEquals(expectedBelongDatacenterId,userRequest.getBelongDatacenterId());

    }
}
