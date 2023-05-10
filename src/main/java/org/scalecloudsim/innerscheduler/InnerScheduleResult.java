package org.scalecloudsim.innerscheduler;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.request.Instance;

import java.util.List;
import java.util.Map;

public class InnerScheduleResult {
    @Getter
    @Setter
    private InnerScheduler innerScheduler;
    @Getter
    @Setter
    Map<Integer, List<Instance>> scheduleResult;
    @Getter
    @Setter
    double scheduleTime;

    public InnerScheduleResult(InnerScheduler innerScheduler) {
        this.innerScheduler = innerScheduler;
    }
}
