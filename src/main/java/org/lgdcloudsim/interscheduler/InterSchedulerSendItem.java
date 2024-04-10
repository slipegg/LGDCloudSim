package org.lgdcloudsim.interscheduler;

import lombok.Getter;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.util.FailedOutdatedResult;

import java.util.List;

/**
 * When the CIS's centralized inter-scheduler schedules the instance to the host,
 * this class is needed to store the information to be sent to each data center with the {@link InterScheduler} and {@link #instanceGroups}.
 * After receiving the scheduling results, each data center also needs to use this class to store instance groups
 * that failed to schedule and user requests that exceeded the scheduling time limit
 * with the {@link InterScheduler} and {@link FailedOutdatedResult} class,
 * and send them back to the corresponding inter-scheduler.
 * The key use of this class is to additionally store inter-scheduler,
 * so requests that fail to be scheduled can be returned to the corresponding inter-scheduler.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
public class InterSchedulerSendItem {
    /**
     * The inter-scheduler for scheduling the instance group.
     */
    InterScheduler interScheduler;

    /**
     * The scheduled instance groups.
     */
    List<InstanceGroup> instanceGroups;

    /**
     * The failed instance groups and user requests that exceed the scheduling time limit.
     */
    FailedOutdatedResult<InstanceGroup> failedOutdatedResult;

    /**
     * Constructed with the inter-scheduler and instance groups.
     * It is used for CIS's centralized inter-scheduler to send instance groups to each data center.
     *
     * @param interScheduler the inter-scheduler
     * @param instanceGroups the instance groups
     */
    public InterSchedulerSendItem(InterScheduler interScheduler, List<InstanceGroup> instanceGroups) {
        this.interScheduler = interScheduler;
        this.instanceGroups = instanceGroups;
    }

    /**
     * Constructed with the inter-scheduler and failed instance groups and user requests.
     * It is used for each data center to send failed instance groups and user requests that exceed the scheduling time limit
     * to the corresponding inter-scheduler.
     * @param interScheduler the inter-scheduler
     * @param failedOutdatedResult the failed instance groups and user requests
     */
    public InterSchedulerSendItem(InterScheduler interScheduler, FailedOutdatedResult<InstanceGroup> failedOutdatedResult) {
        this.interScheduler = interScheduler;
        this.failedOutdatedResult = failedOutdatedResult;
    }
}
