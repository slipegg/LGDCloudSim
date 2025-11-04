package org.lgdcloudsim.statemanager.simplestate;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

/**
 * The class is the object sent to the inter-scheduler during synchronization.
 * It contains the following information:
 * <ul>
 *     <li>The number of hosts in the datacenter</li>
 *     <li>The sum of the available cpu of all hosts in the datacenter</li>
 *     <li>The sum of the available ram of all hosts in the datacenter</li>
 *     <li>The sum of the available storage of all hosts in the datacenter</li>
 *     <li>The sum of the available bw of all hosts in the datacenter</li>
 *     <li>The sum of the total cpu capacity of all hosts in the datacenter</li>
 *     <li>The sum of the total ram capacity of all hosts in the datacenter</li>
 *     <li>The sum of the total storage capacity of all hosts in the datacenter</li>
 *     <li>The sum of the total bw capacity of all hosts in the datacenter</li>
 * </ul>
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
@Setter
public class SimpleStateEasyObject {
    /**
     * The number of hosts in the datacenter.
     */
    int hostNum;
    /**
     * The sum of the available cpu of all hosts in the datacenter.
     */
    long cpuAvailableSum;
    /**
     * The sum of the available ram of all hosts in the datacenter.
     */
    long ramAvailableSum;
    /**
     * The sum of the available storage of all hosts in the datacenter.
     */
    long storageAvailableSum;
    /**
     * The sum of the available bw of all hosts in the datacenter.
     */
    long bwAvailableSum;
    /**
     * The sum of the available gpu of all hosts in the datacenter.
     */
    long gpuAvailableSum;
    /**
     * The sum of the total cpu capacity of all hosts in the datacenter.
     */
    long cpuCapacitySum;
    /**
     * The sum of the total ram capacity of all hosts in the datacenter.
     */
    long ramCapacitySum;
    /**
     * The sum of the total storage capacity of all hosts in the datacenter.
     */
    long storageCapacitySum;
    /**
     * The sum of the total bw capacity of all hosts in the datacenter.
     */
    long bwCapacitySum;
    /**
     * The sum of the available gpu of all hosts in the datacenter.
     */
    HashMap<String, Long> gpuAvailableSumMap;
    /**
     * The sum of the total gpu capacity of all hosts in the datacenter.
     */
    HashMap<String, Long> gpuCapacitySumMap;
    /**
     * Construct a simple state easy object with the number of hosts in the datacenter, the sum of the available resources and the sum of the total resources.
     *
     * @param hostNum             the number of hosts in the datacenter.
     * @param cpuAvailableSum     the sum of the available cpu of all hosts in the datacenter.
     * @param ramAvailableSum     the sum of the available ram of all hosts in the datacenter.
     * @param storageAvailableSum the sum of the available storage of all hosts in the datacenter.
     * @param bwAvailableSum      the sum of the available bw of all hosts in the datacenter.
     * @param cpuCapacitySum      the sum of the total cpu capacity of all hosts in the datacenter.
     * @param ramCapacitySum      the sum of the total ram capacity of all hosts in the datacenter.
     * @param storageCapacitySum  the sum of the total storage capacity of all hosts in the datacenter.
     * @param bwCapacitySum       the sum of the total bw capacity of all hosts in the datacenter.
     */
    public SimpleStateEasyObject(int hostNum, long cpuAvailableSum, long ramAvailableSum, long storageAvailableSum, long bwAvailableSum, long gpuAvailableSum, HashMap<String, Long> gpuAvailableSumMap,
                                long cpuCapacitySum, long ramCapacitySum, long storageCapacitySum, long bwCapacitySum, HashMap<String, Long> gpuCapacitySumMap) {
        this.hostNum = hostNum;
        this.cpuAvailableSum = cpuAvailableSum;
        this.ramAvailableSum = ramAvailableSum;
        this.storageAvailableSum = storageAvailableSum;
        this.bwAvailableSum = bwAvailableSum;
        this.gpuAvailableSum = gpuAvailableSum;
        this.gpuAvailableSumMap = gpuAvailableSumMap;
        this.cpuCapacitySum = cpuCapacitySum;
        this.ramCapacitySum = ramCapacitySum;
        this.storageCapacitySum = storageCapacitySum;
        this.bwCapacitySum = bwCapacitySum;
        this.gpuCapacitySumMap = gpuCapacitySumMap;
    }

    /**
     * When inter-scheduler schedules a user request to a data center,
     * it can use this function to update the available resources of the datacenter.
     *
     * @param cpu the cpu to be allocated.
     * @param ram the ram to be allocated.
     * @param storage the storage to be allocated.
     * @param bw the bw to be allocated.
     */
    public void allocateResource(long cpu, long ram, long storage, long bw, long gpu, String gpuType) {
        cpuAvailableSum -= cpu;
        ramAvailableSum -= ram;
        storageAvailableSum -= storage;
        bwAvailableSum -= bw;
        gpuAvailableSum -= gpu;
        gpuAvailableSumMap.put(gpuType, gpuAvailableSumMap.getOrDefault(gpuType, 0L) - gpu);    
    }
}
