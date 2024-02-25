package org.cpnsim.datacenter;

import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.util.List;

/**
 * An interface to be implemented by each class that represents a instanceGroup queue.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface InstanceGroupQueue {
    /**
     * Add a list of userRequests to the queue.
     *
     * @param userRequestsOrInstanceGroups the list of userRequests to be added to the queue
     */
    InstanceGroupQueue add(List<?> userRequestsOrInstanceGroups);

    /**
     * Add a userRequest to the queue.
     *
     * @param userRequest the userRequest to be added to the queue
     */
    InstanceGroupQueue add(UserRequest userRequest);

    /**
     * Add a instanceGroup to the queue.
     *
     * @param instanceGroup the instanceGroup to be added to the queue
     */
    InstanceGroupQueue add(InstanceGroup instanceGroup);

    /**
     * Get a batch of groupInstances from the queue.
     */
    QueueResult<InstanceGroup> getBatchItem(double nowTime);

    QueueResult<InstanceGroup> getItems(int num, double nowTime);

    /**
     * Get the size of the queue.
     */
    int size();

    /**
     * Get the number of instanceGroups to be sent in a batch.
     */
    int getBatchNum();

    /**
     * Set the number of instanceGroups to be sent in a batch.
     */
    InstanceGroupQueue setBatchNum(int batchNum);

    boolean isEmpty();

    InstanceGroupQueue setCheckOutdatedFlag(boolean checkOutdatedFlag);

    boolean isCheckOutdatedFlag();
}
