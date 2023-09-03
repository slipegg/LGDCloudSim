package org.cpnsim.statemanager;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Map;

/**
 * A class to record the overall state information of hosts in an entire datacenter
 * These host states are used for scheduling by {@link org.cpnsim.interscheduler.InterScheduler}.
 * It contains the following information:
 * <ul>
 *     <li>The sum of the cpu of all hosts in the datacenter</li>
 *     <li>The sum of the ram of all hosts in the datacenter</li>
 *     <li>The sum of the storage of all hosts in the datacenter</li>
 *     <li>The sum of the bw of all hosts in the datacenter</li>
 *     <li>The number of hosts whose remaining resources are greater than this combination of (cpu, ram)</li>
 *     <li>The number of hosts whose remaining resources are between (cpu1, ram1) combination and (cpu2, ram2) combination</li>
 * </ul>
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface SimpleState {
    /**
     * Update the storage state of the datacenter simple state.
     *
     * @param changeStorage The storage change of the host.
     * @return the updated state of the host.The state includes 4 integers: cpu, ram, storage and bw.
     */
    SimpleState updateStorageSum(int changeStorage);

    /**
     * Update the bw state of the datacenter simple state.
     *
     * @param changeBw The bw change of the host.
     * @return the updated state of the host.The state includes 4 integers: cpu, ram, storage and bw.
     */
    SimpleState updateBwSum(int changeBw);

    /**
     * Add a cpu ram record to the datacenter simple state.
     *
     * @param cpu The cpu change of the host.
     * @return the updated state of the host.The state includes 4 integers: cpu, ram, storage and bw.
     */
    SimpleState addCpuRamRecord(int cpu, int ram);

    /**
     * update the cpu ram record to the datacenter simple state.
     *
     * @param originRam The origin ram of the host.
     * @param originCpu The origin cpu of the host.
     * @param nowRam    The now ram of the host.
     * @param nowCpu    The now cpu of the host.
     * @return the updated state of the host.The state includes 4 integers: cpu, ram, storage and bw.
     */
    SimpleState updateCpuRamMap(int originCpu, int originRam, int nowCpu, int nowRam);

    /**
     * Get the available cpu sum of the datacenter simple state.
     *
     * @return the available cpu sum of the datacenter simple state.
     */
    long getCpuAvaiableSum();

    /**
     * Get the available ram sum of the datacenter simple state.
     *
     * @return the available ram sum of the datacenter simple state.
     */
    long getRamAvaiableSum();

    /**
     * Get the available storage sum of the datacenter simple state.
     *
     * @return the available storage sum of the datacenter simple state.
     */
    long getStorageAvaiableSum();

    /**
     * Get the available bw sum of the datacenter simple state.
     *
     * @return the available bw sum of the datacenter simple state.
     */
    long getBwAvaiableSum();

    /**
     * Get the number of hosts whose remaining resources are greater than this combination of (cpu, ram).
     *
     * @param cpu The cpu of the host.
     * @param ram The ram of the host.
     * @return the number of hosts whose remaining resources are greater than this combination of (cpu, ram).
     */
    int getCpuRamSum(int cpu, int ram);
}
