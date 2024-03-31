package org.lgdcloudsim.queue;


import lombok.Getter;
import org.lgdcloudsim.request.UserRequest;

import java.util.List;
import java.util.Set;

/**
 * A class to record the results of queue selection
 * It contains the list of instances to be scheduled
 * and the set of requests that has exceeded the maximum schedule delay limit and need to be marked as failed.
 *
 * @param <T> the type of the failed request. It can be {@link org.lgdcloudsim.request.Instance} or {@link org.lgdcloudsim.request.InstanceGroup}
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class QueueResult<T> {
    /**
     * The list of instances to be scheduled
     */
    @Getter
    List<T> waitScheduledItems;

    /**
     * The set of requests that has exceeded the maximum schedule delay limit and need to be marked as failed.
     */
    @Getter
    Set<UserRequest> outDatedItems;

    /**
     * Create a new instance of QueueResult with an empty list of instances to be scheduled and an empty set of requests that has exceeded the maximum schedule delay limit.
     *
     * @param waitScheduledItems the list of instances to be scheduled
     * @param outDatedItems      the set of requests that has exceeded the maximum schedule delay limit
     */
    public QueueResult(List<T> waitScheduledItems, Set<UserRequest> outDatedItems) {
        this.waitScheduledItems = waitScheduledItems;
        this.outDatedItems = outDatedItems;
    }

    /**
     * Add a queue result to the current queue result
     *
     * @param queueResult the queue result to be added
     * @return the current queue result
     */
    public QueueResult add(QueueResult queueResult) {
        this.waitScheduledItems.addAll(queueResult.getWaitScheduledItems());
        this.outDatedItems.addAll(queueResult.getOutDatedItems());
        return this;
    }

    /**
     * Get the size of the list of instances to be scheduled
     *
     * @return the size of the list of instances to be scheduled
     */
    public int getWaitScheduledItemsSize() {
        return waitScheduledItems.size();
    }

}
