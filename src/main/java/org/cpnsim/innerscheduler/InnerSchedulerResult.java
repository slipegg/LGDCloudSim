package org.cpnsim.innerscheduler;

import lombok.Getter;
import org.cpnsim.request.Instance;

import java.util.ArrayList;
import java.util.List;

public class InnerSchedulerResult {
    @Getter
    private InnerScheduler innerScheduler;

    @Getter
    private List<Instance> scheduledInstances;

    @Getter
    private double scheduleTime;

    @Getter
    private List<Instance> failedInstances;

    public InnerSchedulerResult(InnerScheduler innerScheduler, double scheduleTime) {
        this.innerScheduler = innerScheduler;
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
        return failedInstances.isEmpty();
    }
}
