package org.lgdcloudsim.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;

public class utils {
        public static List<InstanceGroup> groupToInstanceGroups(List<Instance> instances) {
        Map<InstanceGroup, List<Instance>> instanceMap = new HashMap<>();
        for (Instance instance : instances) {
            InstanceGroup instanceGroup = instance.getInstanceGroup();
            instanceMap.putIfAbsent(instanceGroup, new ArrayList<>());
            instanceMap.get(instanceGroup).add(instance);
        }

        for (Map.Entry<InstanceGroup, List<Instance>> entry : instanceMap.entrySet()) {
            InstanceGroup group = entry.getKey();
            List<Instance> groupInstances = entry.getValue();
            
            if (group.getInstances().size() != groupInstances.size()) {
                throw new RuntimeException(String.format("InstanceGroup-%d size mismatch. The scheduled instances size is %d, but the InstanceGroup size is %d.", group.getId(), groupInstances.size(), group.getInstances().size()));
            }
        }

        return new ArrayList<>(instanceMap.keySet());
    }
}
