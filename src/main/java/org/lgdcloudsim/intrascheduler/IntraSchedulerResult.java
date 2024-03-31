package org.lgdcloudsim.intrascheduler;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.UserRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The result of the intra-scheduler scheduling.
 * It contains the intra-scheduler, the scheduled instances, the schedule time, and the failed instances.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
public class IntraSchedulerResult {
    /**
     * The intra-scheduler that schedules the instances.
     */
    private IntraScheduler intraScheduler;

    /**
     * The scheduled instances.
     * The schedule result is stored in the {@link Instance#getExpectedScheduleHostId()} field.
     */
    private List<Instance> scheduledInstances;

    /**
     * The time spent on the scheduling.
     */
    private double scheduleTime;

    /**
     * The failed instances.
     */
    private List<Instance> failedInstances;

    /**
     * The out-dated user requests.
     */
    @Setter
    private Set<UserRequest> outDatedUserRequests;

    /**
     * Construct the intra-scheduler result with the intra-scheduler and the schedule time.
     *
     * @param intraScheduler the intra-scheduler that schedules the instances.
     * @param scheduleTime   the time spent on the scheduling.
     */
    public IntraSchedulerResult(IntraScheduler intraScheduler, double scheduleTime) {
        this.intraScheduler = intraScheduler;
        this.scheduleTime = scheduleTime;
        this.scheduledInstances = new ArrayList<>();
        this.failedInstances = new ArrayList<>();
    }

    /**
     * Add the scheduled instance to the scheduled instances list.
     * @param instance the scheduled instance.
     */
    public void addScheduledInstance(Instance instance) {
        this.scheduledInstances.add(instance);
    }

    /**
     * Add the failed instance to the failed instances list.
     * @param instance the failed instance.
     */
    public void addFailedScheduledInstance(Instance instance) {
        this.failedInstances.add(instance);
    }

    /**
     * Get the number of the scheduled instances and the failed instances.
     * @return the number of the scheduled instances and the failed instances.
     */
    public int getInstanceNum() {
        return scheduledInstances.size() + failedInstances.size();
    }

    /**
     * Get whether the scheduled instances list is empty.
     * @return whether the scheduled instances list is empty.
     */
    public boolean isScheduledInstancesEmpty() {
        return scheduledInstances.isEmpty();
    }

    /**
     * Get whether the failed instances list is empty.
     * @return whether the failed instances list is empty.
     */
    public boolean isFailedInstancesEmpty() {
        return failedInstances.isEmpty() && outDatedUserRequests.isEmpty();
    }
}
