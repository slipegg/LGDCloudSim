package org.lgdcloudsim.interscheduler;

import lombok.Getter;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.util.FailedOutdatedResult;

import java.util.List;

@Getter
public class InterSchedulerSendItem {
    InterScheduler interScheduler;
    List<InstanceGroup> instanceGroups;
    FailedOutdatedResult<InstanceGroup> failedOutdatedResult;

    public InterSchedulerSendItem(InterScheduler interScheduler, List<InstanceGroup> instanceGroups) {
        this.interScheduler = interScheduler;
        this.instanceGroups = instanceGroups;
    }

    public InterSchedulerSendItem(InterScheduler interScheduler, FailedOutdatedResult<InstanceGroup> failedOutdatedResult) {
        this.interScheduler = interScheduler;
        this.failedOutdatedResult = failedOutdatedResult;
    }
}
