package org.cpnsim.statemanager;

import org.apache.commons.lang3.mutable.MutableInt;
import org.cpnsim.request.Instance;

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
    SimpleState initHostSimpleState(int hostId, int[] hostState);

    SimpleState updateSimpleStateAllocated(int hostId, int[] hostState, Instance instance);

    SimpleState updateSimpleStateReleased(int hostId, int[] hostState, Instance instance);

    long getCpuAvailableSum();

    long getRamAvailableSum();

    long getStorageAvailableSum();

    long getBwAvailableSum();

    Object clone();
}
