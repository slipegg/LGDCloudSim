package org.cpnsim.datacenter;

import lombok.Getter;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;

import java.util.List;
import java.util.Map;

public class ResourceAllocateResult {
    @Getter
    private Map<InnerScheduler, List<Instance>> successRes;
    @Getter
    private Map<InnerScheduler, List<Instance>> failRes;

    public ResourceAllocateResult(Map<InnerScheduler, List<Instance>> successRes, Map<InnerScheduler, List<Instance>> failRes) {
        this.successRes = successRes;
        this.failRes = failRes;
    }

}
