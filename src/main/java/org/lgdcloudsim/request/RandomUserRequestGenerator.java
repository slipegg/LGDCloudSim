package org.lgdcloudsim.request;

import java.util.*;

/**
 * A simple user request generator to randomly generate a user request.
 * It is the implementation of the {@link UserRequestGenerator} interface.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class RandomUserRequestGenerator implements UserRequestGenerator {
    Random rand = new Random(0);

    int maxInstanceGroupNum = 5;

    int minInstanceGroupNum = 1;

    int maxInstanceNumInGroup = 10;

    int minInstanceNumInGroup = 1;

    static int userRequestId = 0;

    static int instanceId = 0;

    static int instanceGroupId = 0;

    @Override
    public UserRequest generateAUserRequest() {
        List<InstanceGroup> instanceGroups = generateInstanceGroups();

        InstanceGroupGraph instanceGroupGraph = generateInstanceGroupGraph(instanceGroups);

        return new UserRequestSimple(userRequestId++, instanceGroups, instanceGroupGraph, "China").setBelongDatacenterId(1);
    }

    /**
     * Generate instance groups for a user request.
     *
     * @return a list of instance groups.
     */
    private List<InstanceGroup> generateInstanceGroups() {
        List<InstanceGroup> instanceGroups = new ArrayList<>();

        int instanceGroupNum = rand.nextInt(maxInstanceGroupNum - minInstanceGroupNum + 1) + minInstanceGroupNum;

        for (int j = 0; j < instanceGroupNum; j++) {
            InstanceGroup instanceGroup = new InstanceGroupSimple(instanceGroupId++);
            List<Instance> instances = generateInstances();
            instanceGroup.setInstances(instances);

            instanceGroup.setAccessLatency(rand.nextDouble(100) + 200);

            instanceGroups.add(instanceGroup);
        }

        return instanceGroups;
    }

    /**
     * Generate instances for an instance group.
     *
     * @return a list of instances.
     */
    private List<Instance> generateInstances() {
        int instanceNum = rand.nextInt(maxInstanceNumInGroup - minInstanceNumInGroup + 1) + minInstanceNumInGroup;

        List<Instance> instances = new ArrayList<>();
        for (int i = 0; i < instanceNum; i++) {
            Instance instance = new InstanceSimple(instanceId++, i, i, i, i, 3000);
            instances.add(instance);
        }

        return instances;
    }

    /**
     * Generate an instance group graph for a user request.
     *
     * @param instanceGroups the instance groups in the user request.
     * @return the instance group graph.
     */
    private InstanceGroupGraph generateInstanceGroupGraph(List<InstanceGroup> instanceGroups) {
        InstanceGroupGraph instanceGroupGraph = new InstanceGroupGraphSimple(false);

        int instanceGroupNum = instanceGroups.size();
        int maxEdgeNum = (instanceGroupNum - 1) * instanceGroupNum / 2;

        if (maxEdgeNum < 3) {
            return instanceGroupGraph;
        }

        int srcInstanceGroupId = rand.nextInt(instanceGroupNum);
        Set<Integer> dstInstanceGroupIds = new HashSet<>();

        for (int i = 0; i < 3; i++) {
            int dstInstanceGroupId = rand.nextInt(instanceGroupNum);
            while (dstInstanceGroupIds.contains(dstInstanceGroupId) || dstInstanceGroupId == srcInstanceGroupId) {
                dstInstanceGroupId = rand.nextInt(instanceGroupNum);
            }
            dstInstanceGroupIds.add(dstInstanceGroupId);

            InstanceGroupEdge instanceGroupEdge = generateInstanceGroupEdge(instanceGroups.get(srcInstanceGroupId),
                    instanceGroups.get(dstInstanceGroupId));
            instanceGroupGraph.addEdge(instanceGroupEdge);
        }

        return instanceGroupGraph;
    }

    /**
     * Generate an instance group edge between two instance groups.
     *
     * @param src the source instance group.
     * @param dst the destination instance group.
     * @return the instance group edge.
     */
    private InstanceGroupEdge generateInstanceGroupEdge(InstanceGroup src, InstanceGroup dst) {
        double bandwidth = rand.nextDouble(100);
        double latency = rand.nextDouble(200) + 400;
        return new InstanceGroupEdgeSimple(src, dst, latency, bandwidth);
    }
}
