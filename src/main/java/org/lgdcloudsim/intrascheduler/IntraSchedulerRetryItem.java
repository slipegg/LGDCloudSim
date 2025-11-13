package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.request.Instance;

import lombok.Getter;

import java.util.List;

@Getter
public class IntraSchedulerRetryItem {
    private IntraScheduler intraScheduler;
    private List<Instance> retryInstances;
    
    public IntraSchedulerRetryItem(IntraScheduler intraScheduler, List<Instance> retryInstances) {
        this.intraScheduler = intraScheduler;
        this.retryInstances = retryInstances;
    }
}
