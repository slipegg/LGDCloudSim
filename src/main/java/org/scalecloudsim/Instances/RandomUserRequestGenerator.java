package org.scalecloudsim.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomUserRequestGenerator implements UserRequestGenerator{
    static int userRequestId=0;
    static int instanceId=0;
    static int instanceGroupId=0;
    @Override
    public UserRequest generateAUserRequest(){
        Random rand=new Random(0);

        List<InstanceGroup> instanceGroups=new ArrayList<>();

        int maxInstanceGroupNum = 5;
        int instanceGroupNum = rand.nextInt(maxInstanceGroupNum) + 2;

        int maxInstanceNumInGroup = 10;

        for (int j = 0; j < instanceGroupNum; j++) {
            int instanceNum = rand.nextInt(maxInstanceNumInGroup) + 1;

            InstanceGroup instanceGroup = new InstanceGroupSimple(instanceGroupId++);
            List<Instance> instances = new ArrayList<>();
            for (int i = 0; i < instanceNum; i++) {
                Instance instance = new InstanceSimple(instanceId++, i, i, i, i);
                instances.add(instance);
            }
            instanceGroup.setInstanceList(instances);
            instanceGroup.setAcessLatency(rand.nextDouble(1.0));
            instanceGroups.add(instanceGroup);
        }

        InstanceGroupGraph instanceGroupGraph=new InstanceGroupGraphSimple(false);
        int maxEdgeNum = instanceGroupNum*(instanceGroupNum-1)/2;
        int edgeNum = rand.nextInt(maxEdgeNum) + 1;
        for(int i=0;i<edgeNum;i++){//可能会有重复的边，但是因为是set，所以不会有影响
            int from = rand.nextInt(instanceGroupNum);
            int to = rand.nextInt(instanceGroupNum);
            while (from == to) {
                to = rand.nextInt(instanceGroupNum);
            }
            double bandwidth = rand.nextDouble(10);
            double latency = rand.nextDouble(10);
            InstanceGroupEdge instanceGroupEdge = new InstanceGroupEdgeSimple(instanceGroups.get(from), instanceGroups.get(to), bandwidth, latency);
            instanceGroupGraph.addEdge(instanceGroupEdge);
        }

        UserRequest userRequest = new UserRequestSimple(userRequestId++, instanceGroups, instanceGroupGraph);
        return userRequest;
    }
}
