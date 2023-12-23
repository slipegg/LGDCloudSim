package org.cpnsim.datacenter;

import lombok.Getter;
import org.cpnsim.intrascheduler.IntraScheduler;
import org.cpnsim.request.Instance;

import java.util.List;
import java.util.Map;

public class ResourceAllocateResult {
    @Getter
    private Map<IntraScheduler, List<Instance>> successRes;
    @Getter
    private Map<IntraScheduler, List<Instance>> failRes;

    public ResourceAllocateResult(Map<IntraScheduler, List<Instance>> successRes, Map<IntraScheduler, List<Instance>> failRes) {
        this.successRes = successRes;
        this.failRes = failRes;
    }

}
