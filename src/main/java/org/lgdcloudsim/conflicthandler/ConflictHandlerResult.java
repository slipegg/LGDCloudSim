package org.lgdcloudsim.conflicthandler;

import lombok.Getter;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.UserRequest;
import org.lgdcloudsim.util.FailedOutdatedResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The result of the conflict handler.
 * It contains the success result that the instances are allocated to the host successfully,
 * and the failed result that the instances need to be rescheduled or be marked as failed.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class ConflictHandlerResult {
    /**
     * The success result that the instances are allocated to the host successfully.
     * The key is the intra-scheduler, and the value is the instances allocated to the host successfully.
     */
    @Getter
    private Map<IntraScheduler, List<Instance>> successRes;

    /**
     * The conflicted result that the instances need to be rescheduled or be marked as failed.
     */
    @Getter
    private Map<IntraScheduler, FailedOutdatedResult> failedOutdatedResultMap;

    /**
     * Construct a new ConflictHandlerResult.
     */
    public ConflictHandlerResult() {
        this.successRes = new HashMap<>();
        this.failedOutdatedResultMap = new HashMap<>();
    }

    /**
     * Add the success result that the instances are allocated to the host successfully.
     *
     * @param intraScheduler the intra-scheduler
     * @param instances      the instances allocated to the host successfully
     */
    public void addSuccessRes(IntraScheduler intraScheduler, List<Instance> instances) {
        this.successRes.put(intraScheduler, instances);
    }

    /**
     * Add the conflicted result that the instances need to be rescheduled or be marked as failed.
     *
     * @param failedAllocatedRes the failed allocated result
     */
    public void addAllocateFailRes(Map<IntraScheduler, List<Instance>> failedAllocatedRes) {
        for (Map.Entry<IntraScheduler, List<Instance>> entry : failedAllocatedRes.entrySet()) {
            this.failedOutdatedResultMap.get(entry.getKey()).getFailRes().addAll(entry.getValue());
        }
    }

    /**
     * Add the conflicted result that need to be rescheduled or be marked as failed,
     * and the user requests that have exceeded the scheduling time limit.
     * The user requests that have exceeded the scheduling time limit will be marked as failed directly.
     * It is used to deal with the intra-scheduling results.
     *
     * @param intraScheduler the intra-scheduler
     * @param instances      the conflicted instances that need to be rescheduled or be marked as failed
     * @param userRequests   the user requests that have exceeded the scheduling time limit
     */
    public void addFailRes(IntraScheduler intraScheduler, List<Instance> instances, Set<UserRequest> userRequests) {
        this.failedOutdatedResultMap.put(intraScheduler, new FailedOutdatedResult(instances, userRequests));
    }
}
