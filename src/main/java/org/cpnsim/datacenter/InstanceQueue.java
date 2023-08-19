package org.cpnsim.datacenter;

import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;

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
    List<Instance> getBatchItem();

    /**
     * Get all instances from the queue.
     */
    List<Instance> getAllItem();

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
     * Add a list of instances to the queue.
     *
     * @param instances the list of instances to be added to the queue
     */
    InstanceQueue add(List instances);
}
