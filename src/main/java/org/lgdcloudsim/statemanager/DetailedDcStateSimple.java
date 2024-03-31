package org.lgdcloudsim.statemanager;

import lombok.Getter;
import org.lgdcloudsim.request.Instance;

/**
 * Used to describe the status of a data center,
 * including the status of each host and the total resource status of the data center.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class DetailedDcStateSimple {
    /**
     * The status of each host in the data center.
     */
    int[] hostStates;

    /**
     * The host capacity manager which records the capacity of each host in the data center.
     */
    final HostCapacityManager hostCapacityManager;

    /**
     * The number of hosts in the data center.
     */
    @Getter
    final int hostNum;

    /**
     * The total available CPU resources of the data center.
     */
    @Getter
    long cpuAvailableSum;

    /**
     * The total available RAM resources of the data center.
     */
    @Getter
    long ramAvailableSum;

    /**
     * The total available storage resources of the data center.
     */
    @Getter
    long storageAvailableSum;

    /**
     * The total available bandwidth resources of the data center.
     */
    @Getter
    long bwAvailableSum;

    /**
     * Construct a new DetailedDcStateSimple.
     *
     * @param hostStates          The status of each host in the data center.
     * @param hostCapacityManager The host capacity manager which records the capacity of each host in the data center.
     * @param cpuAvailableSum     The total available CPU resources of the data center.
     * @param ramAvailableSum     The total available RAM resources of the data center.
     * @param storageAvailableSum The total available storage resources of the data center.
     * @param bwAvailableSum      The total available bandwidth resources of the data center.
     */
    public DetailedDcStateSimple(int[] hostStates, HostCapacityManager hostCapacityManager, long cpuAvailableSum, long ramAvailableSum, long storageAvailableSum, long bwAvailableSum) {
        this.hostStates = hostStates.clone();//It must be cloned because it will be modified later, and this value must also be independent of the original value.
        this.hostCapacityManager = hostCapacityManager;
        hostNum = hostStates.length / 4;
        this.cpuAvailableSum = cpuAvailableSum;
        this.ramAvailableSum = ramAvailableSum;
        this.storageAvailableSum = storageAvailableSum;
        this.bwAvailableSum = bwAvailableSum;
    }

    /**
     * Get the status of the host synchronized to with the given host id.
     * @param hostId The id of the host.
     * @return The status of the host.
     */
    public HostState getHostState(int hostId) {
        return new HostState(hostStates[hostId * HostState.STATE_NUM], hostStates[hostId * HostState.STATE_NUM + 1], hostStates[hostId * HostState.STATE_NUM + 2], hostStates[hostId * HostState.STATE_NUM + 3]);
    }

    /**
     * Get the capacity of the host with the given host id, including CPU, RAM, storage, and bandwidth.
     * @param hostId The id of the host.
     * @return The capacity of the host.
     */
    public int[] getHostCapacity(int hostId) {
        return hostCapacityManager.getHostCapacity(hostId);
    }

    /**
     * Change the status of the host after allocating the instance to it.
     * It is used to update the status of the host from the scheduler's perspective after the instance is allocated to the host.
     * @param instance The instance to be allocated.
     * @param hostId The id of the host.
     * @return The DetailedDcStateSimple itself.
     */
    public DetailedDcStateSimple allocate(Instance instance, int hostId) {
        hostStates[hostId * HostState.STATE_NUM] -= instance.getCpu();
        hostStates[hostId * HostState.STATE_NUM + 1] -= instance.getRam();
        hostStates[hostId * HostState.STATE_NUM + 2] -= instance.getStorage();
        hostStates[hostId * HostState.STATE_NUM + 3] -= instance.getBw();
        cpuAvailableSum -= instance.getCpu();
        ramAvailableSum -= instance.getRam();
        storageAvailableSum -= instance.getStorage();
        bwAvailableSum -= instance.getBw();
        return this;
    }
}
