package org.lgdcloudsim.queue;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

import java.util.List;

/**
 * An interface to be implemented by each class that represents an instance queue.
 * The instance queue is used to store the instances to be scheduled.
 * Every intra-scheduler has two queues, one stores the instances to be scheduled,
 * and the other stores the instances that need to be rescheduled.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface InstanceQueue {
    /**
     * Get the size of the queue.
     */
    int size();

    /**
     * Get a batch of instances from the queue.
     */
    QueueResult<Instance> getBatchItem(double nowTime);

    /**
     * Get all instances from the queue.
     */
    List<Instance> getAllItem();

    QueueResult<Instance> getItems(int num, double nowTime);

    /**
     * Add a list of instances to the queue.
     *
     * @param instance the list of instances to be added to the queue
     */
    InstanceQueue add(Instance instance);

    /**
     * Add all instances of a instanceGroup to the queue.
     *
     * @param instanceGroup the instance to be added to the queue
     */
    InstanceQueue add(InstanceGroup instanceGroup);

    /**
     * Add all instances of a userRequest to the queue.
     *
     * @param userRequest the userRequest to be added to the queue
     */
    InstanceQueue add(UserRequest userRequest);

    /**
     * Add a list of instances to the queue.
     * The list can be a list of instances, a list of instanceGroups, or a list of userRequests.
     *
     * @param requests the list of instances to be added to the queue
     */
    InstanceQueue add(List requests);

    /**
     * Get the number of instances to be sent in a batch.
     */
    int getBatchNum();

    /**
     * Set the number of instances to be sent in a batch.
     */
    InstanceQueue setBatchNum(int batchNum);

    /**
     * Get whether the queue is empty.
     */
    boolean isEmpty();

    /**
     * Set whether queue needs to check the instance in the queue exceeds the scheduling delay limit when get instances.
     * If the flag is true, when traversing and selecting instances,
     * the traversed instances that have exceeded the scheduling delay limit will be recorded,
     * deleted from the queue, and sent to the handler for scheduling failure.
     * These instances that have exceeded the scheduling delay limit will not be counted.
     * If the flag is false, there is no additional check to see if the instance has exceeded the scheduling delay limit.
     *
     * @param checkOutdatedFlag the flag to check the instance in the queue exceeds the scheduling delay limit
     * @return the instanceQueue
     */
    InstanceQueue setCheckOutdatedFlag(boolean checkOutdatedFlag);

    /**
     * Get whether the queue needs to check the instance in the queue exceeds the scheduling delay limit when get instances.
     *
     * @return true if the queue needs to check the instance in the queue exceeds the scheduling delay limit, false otherwise
     */
    boolean isCheckOutdatedFlag();
}
