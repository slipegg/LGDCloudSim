package org.scalecloudsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.request.UserRequest;
import org.scalecloudsim.statemanager.HostState;
import org.scalecloudsim.statemanager.StateManager;

import java.util.*;

public class ResourceAllocateSelectorSimple implements ResourceAllocateSelector {
    @Getter
    @Setter
    Datacenter datacenter;

    @Override
    public Map<Integer, List<Instance>> selectResourceAllocate(Map<Integer, List<Instance>> scheduleRes) {
        Map<Integer, List<Instance>> res = new HashMap<>();
        StateManager stateManager = datacenter.getStateManager();
        for (Map.Entry<Integer, List<Instance>> entry : scheduleRes.entrySet()) {
            int hostId = entry.getKey();
            List<Instance> instances = entry.getValue();
            HostState hostState = datacenter.getStateManager().getnowHostState(hostId);
            for (Instance instance : instances) {
                if (instance.getUserRequest().getState() == UserRequest.FAILED) {
                    continue;
                }
                if (hostState.isSuitable(instance)) {
                    hostState.allocate(instance);
                    res.putIfAbsent(hostId, new ArrayList<>());
                    res.get(hostId).add(instance);
                } else {
                    res.putIfAbsent(-1, new ArrayList<>());
                    res.get(-1).add(instance);
                }
            }
        }
        return res;
    }
}
