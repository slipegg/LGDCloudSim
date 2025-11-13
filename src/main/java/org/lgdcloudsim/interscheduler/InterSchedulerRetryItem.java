package org.lgdcloudsim.interscheduler;

import org.lgdcloudsim.request.InstanceGroup;

import lombok.Getter;

import java.util.List;

@Getter
public class InterSchedulerRetryItem {
    private InterScheduler interScheduler;
    private List<InstanceGroup> retryInstances;

    public InterSchedulerRetryItem(InterScheduler interScheduler, List<InstanceGroup> retryInstanceGroups) {
        this.interScheduler = interScheduler;
        this.retryInstances = retryInstanceGroups;
    }
}
