package org.cpnsim.interscheduler;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;

import java.util.List;
import java.util.Map;

public class CenterSchedulerResult {
    @Getter
    @Setter
    private int collaborationId;

    @Getter
    @Setter
    private Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacentersMap;

    public CenterSchedulerResult(int collaborationId, Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacentersMap) {
        this.collaborationId = collaborationId;
        this.instanceGroupAvailableDatacentersMap = instanceGroupAvailableDatacentersMap;
    }
}
