package org.cpnsim.datacenter;

import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;
import org.cpnsim.request.UserRequest;

import java.util.List;

/**
 * An interface to be implemented by each class that represents a instance queue.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
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

    InstanceQueue add(UserRequest userRequest);

    /**
     * Add a list of instances to the queue.
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

    boolean isEmpty();
}
