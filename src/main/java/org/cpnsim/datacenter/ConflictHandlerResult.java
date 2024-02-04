package org.cpnsim.datacenter;

import lombok.Getter;
import org.cpnsim.intrascheduler.IntraScheduler;
import org.cpnsim.request.Instance;
import org.cpnsim.request.UserRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConflictHandlerResult {
    @Getter
    private Map<IntraScheduler, List<Instance>> successRes;
    @Getter
    private Map<IntraScheduler, FailedOutdatedResult> failedOutdatedResultMap;

    public ConflictHandlerResult() {
        this.successRes = new HashMap<>();
        this.failedOutdatedResultMap = new HashMap<>();
    }

    public void addSuccessRes(IntraScheduler intraScheduler, List<Instance> instances) {
        this.successRes.put(intraScheduler, instances);
    }

    public void addFailRes(IntraScheduler intraScheduler, List<Instance> instances, Set<UserRequest> userRequests) {
        this.failedOutdatedResultMap.put(intraScheduler, new FailedOutdatedResult(instances, userRequests));
    }

    public void addAllocateFailRes(Map<IntraScheduler, List<Instance>> failedAllocatedRes) {
        for (Map.Entry<IntraScheduler, List<Instance>> entry : failedAllocatedRes.entrySet()) {
            this.failedOutdatedResultMap.get(entry.getKey()).getFailRes().addAll(entry.getValue());
        }
    }
}
