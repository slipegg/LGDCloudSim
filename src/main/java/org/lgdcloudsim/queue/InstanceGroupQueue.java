package org.lgdcloudsim.queue;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

import java.util.List;

/**
 * An interface to be implemented by each class that represents a instanceGroup queue.
 * The instanceGroup queue is used to store the instanceGroups to be scheduled.
 * Every inter-scheduler has two queues, one stores the instanceGroups to be scheduled,
 * and the other stores the instanceGroups that need to be rescheduled.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface InstanceGroupQueue {
    /**
     * Add a list of userRequests to the queue.
     *
     * @param userRequestsOrInstanceGroups the list of userRequests to be added to the queue
     * @return the instanceGroupQueue
     */
    InstanceGroupQueue add(List<?> userRequestsOrInstanceGroups);

    /**
     * Add a userRequest to the queue.
     *
     * @param userRequest the userRequest to be added to the queue
     * @return the instanceGroupQueue
     */
    InstanceGroupQueue add(UserRequest userRequest);

    /**
     * Add a instanceGroup to the queue.
     *
     * @param instanceGroup the instanceGroup to be added to the queue
     * @return the instanceGroupQueue
     */
    InstanceGroupQueue add(InstanceGroup instanceGroup);

    /**
     * Get all instance groups from the queue.
     */
    List<InstanceGroup> getAllItem();

    /**
     * Get a batch of groupInstances from the queue.
     *
     * @param nowTime the current time
     * @return the batch of groupInstances
     */
    QueueResult<InstanceGroup> getBatchItem(double nowTime);

    /**
     * Get a specified number of InstanceGroup from the queue.
     *
     * @param num     the number of InstanceGroup
     * @param nowTime the current time
     * @return the batch of groupInstances
     */
    QueueResult<InstanceGroup> getItems(int num, double nowTime);

    /**
     * Get the size of the instanceGroup in queue.
     *
     * @return the size of the instanceGroup in queue
     */
    int size();

    /**
     * Get the number of instanceGroups to be got in a batch.
     *
     * @return the number of instanceGroups to be got in a batch
     */
    int getBatchNum();

    /**
     * Set the number of instanceGroups to be got in a batch.
     *
     * @param batchNum the number of instanceGroups to be got in a batch
     * @return the instanceGroupQueue
     */
    InstanceGroupQueue setBatchNum(int batchNum);

    /**
     * Get whether the queue is empty.
     * @return true if the queue is empty, false otherwise
     */
    boolean isEmpty();

    /**
     * Set whether queue needs to check the instanceGroup in the queue exceeds the scheduling delay limit when get instance groups.
     * If the flag is true, when traversing and selecting instance groups,
     * the traversed instance groups that have exceeded the scheduling delay limit will be recorded,
     * deleted from the queue, and sent to the handler for scheduling failure.
     * These instances that have exceeded the scheduling delay limit will not be counted.
     * If the flag is false, there is no additional check to see if the instance group has exceeded the scheduling delay limit.
     *
     * @param checkOutdatedFlag the flag to check the instanceGroup in the queue exceeds the scheduling delay limit
     * @return the instanceGroupQueue
     */
    InstanceGroupQueue setCheckOutdatedFlag(boolean checkOutdatedFlag);

    /**
     * Get whether queue needs to check the instanceGroup in the queue exceeds the scheduling delay limit when get instance groups.
     * @return true if the queue needs to check the instanceGroup in the queue exceeds the scheduling delay limit, false not to check
     */
    boolean isCheckOutdatedFlag();
}
