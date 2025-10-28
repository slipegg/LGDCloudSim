package org.lgdcloudsim.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceTopologyP2P implements InstanceTopology {
    private Map<Instance, List<Instance>> topologyMap;

    public InstanceTopologyP2P() {
        this.topologyMap = new HashMap<>();
    }

    // 注意是无向图
    public InstanceTopologyP2P addLink(Instance instance, List<Instance> linkedInstances) {
        if (!topologyMap.containsKey(instance)) {
            topologyMap.put(instance, linkedInstances);
        } else {
            List<Instance> existingLinks = topologyMap.get(instance);
            for (Instance linkedInstance : linkedInstances) {
                if (!existingLinks.contains(linkedInstance)) {
                    existingLinks.add(linkedInstance);
                }
            }
        }

        for (Instance linkedInstance : linkedInstances) {
            if (!topologyMap.containsKey(linkedInstance)) {
                // Use a mutable list here. Previously List.of(instance) returned an immutable list
                // which caused UnsupportedOperationException when later adding more links.
                List<Instance> init = new ArrayList<>();
                init.add(instance);
                topologyMap.put(linkedInstance, init);
            } else {
                List<Instance> existingLinks = topologyMap.get(linkedInstance);
                if (!existingLinks.contains(instance)) {
                    existingLinks.add(instance);
                }
            }
        }

        return this;
    }

    @Override
    public Instance getMaxScoreInstance() {
        int maxDegree = -1;
        Instance maxInstance = null;
        for (Instance instance : topologyMap.keySet()) {
            int degree = topologyMap.get(instance).size();
            if (degree > maxDegree && instance.getExpectedScheduleHostId() < 0) {
                maxDegree = degree;
                maxInstance = instance;
            }
        }
        return maxInstance;
    }

    @Override
    public List<Instance> getLinkedInstances(Instance instance) {
        // Return a mutable list copy to avoid callers accidentally modifying internal structures
        // or to allow them to use the returned list safely. If you want a read-only view,
        // change this to Collections.unmodifiableList(...).
        return new ArrayList<>(topologyMap.getOrDefault(instance, List.of()));
    }

    @Override
    public List<Instance> getAllInstances() {
        return new ArrayList<>(topologyMap.keySet());
    }

    @Override
    public String getType() {
        return P2P;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InstanceTopologyP2P{\n");
        for (Map.Entry<Instance, List<Instance>> entry : topologyMap.entrySet()) {
            sb.append("  Instance ").append(entry.getKey().getId()).append(" linked to: ");
            for (Instance linkedInstance : entry.getValue()) {
                sb.append(linkedInstance.getId()).append(" ");
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
