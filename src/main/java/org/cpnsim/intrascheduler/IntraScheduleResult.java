package org.cpnsim.intrascheduler;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.Instance;

import java.util.List;
import java.util.Map;

public class IntraScheduleResult {
    @Getter
    @Setter
    private IntraScheduler intraScheduler;
    @Getter
    @Setter
    Map<Integer, List<Instance>> scheduleResult;
    @Getter
    @Setter
    double scheduleTime;

    public IntraScheduleResult(IntraScheduler intraScheduler) {
        this.intraScheduler = intraScheduler;
    }
}
