package org.lgdcloudsim.statemanager;

import org.lgdcloudsim.request.Instance;

/**
 * A class to record the simple overall state information of hosts in an entire datacenter
 * These host states are used for scheduling by {@link org.lgdcloudsim.interscheduler.InterScheduler}.
 * It must contain the following information:
 * <ul>
 *     <li>The sum of the cpu of all hosts in the datacenter</li>
 *     <li>The sum of the ram of all hosts in the datacenter</li>
 *     <li>The sum of the storage of all hosts in the datacenter</li>
 *     <li>The sum of the bw of all hosts in the datacenter</li>
 * </ul>
 * Other content can be added as needed.
 * The simple state needs to be updated when the instance is allocated to the host or released from the host.
 * When synchronizing the state to the inter-scheduler,
 * a simple copy of the state needs to be generated through the generate function to return to the inter-scheduler.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface SimpleState {
    /**
     * Initialize the host state.
     *
     * @param hostId    the id of the host.
     * @param hostState the state of the host.
     * @return the host state.
     */
    SimpleState initHostSimpleState(int hostId, int[] hostState);

    /**
     * Update the host state when the instance is allocated to the host.
     *
     * @param hostId the id of the host.
     * @param hostState the state of the host.
     * @param instance the instance.
     * @return the host state.
     */
    SimpleState updateSimpleStateAllocated(int hostId, int[] hostState, Instance instance);

    /**
     * Update the host state when the instance is released from the host.
     *
     * @param hostId the id of the host.
     * @param hostState the state of the host.
     * @param instance the instance.
     * @return the host state.
     */
    SimpleState updateSimpleStateReleased(int hostId, int[] hostState, Instance instance);

    /**
     * Get the sum of the available cpu of all hosts in the datacenter.
     * @return the sum of the available cpu of all hosts in the datacenter.
     */
    long getCpuAvailableSum();

    /**
     * Get the sum of the available ram of all hosts in the datacenter.
     * @return the sum of the available ram of all hosts in the datacenter.
     */
    long getRamAvailableSum();

    /**
     * Get the sum of the available storage of all hosts in the datacenter.
     * @return the sum of the available storage of all hosts in the datacenter.
     */
    long getStorageAvailableSum();

    /**
     * Get the sum of the available bw of all hosts in the datacenter.
     * @return the sum of the available bw of all hosts in the datacenter.
     */
    long getBwAvailableSum();

    /**
     * Generate an object copy of the simple state.
     * This function is used to synchronize the state to the inter-scheduler.
     * Depending on the class that implements the simple state, the object copy can be different.
     * @return a copy of the simple state.
     */
    Object generate();
}
