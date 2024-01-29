package org.cpnsim.intrascheduler;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.Instance;
import org.cpnsim.request.UserRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IntraSchedulerResult {
    @Getter
    private IntraScheduler intraScheduler;

    @Getter
    private List<Instance> scheduledInstances;

    @Getter
    private double scheduleTime;

    @Getter
    private List<Instance> failedInstances;

    @Getter
    @Setter
    private Set<UserRequest> outDatedUserRequests;

    public IntraSchedulerResult(IntraScheduler intraScheduler, double scheduleTime) {
        this.intraScheduler = intraScheduler;
        this.scheduleTime = scheduleTime;
        this.scheduledInstances = new ArrayList<>();
        this.failedInstances = new ArrayList<>();
    }

    public void addScheduledInstance(Instance instance) {
        this.scheduledInstances.add(instance);
    }

    public void addFailedScheduledInstance(Instance instance) {
        this.failedInstances.add(instance);
    }

    public int getInstanceNum() {
        return scheduledInstances.size() + failedInstances.size();
    }

    public boolean isScheduledInstancesEmpty() {
        return scheduledInstances.isEmpty();
    }

    public boolean isFailedInstancesEmpty() {
        return failedInstances.isEmpty() && outDatedUserRequests.isEmpty();
    }
}
