package org.cpnsim.datacenter;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to record the power on information of the datacenter.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class DatacenterPowerOnRecord {
    /**
     * Number of hosts powered on.
     **/
    @Getter
    private Map<Integer, Integer> powerOnHostInstanceNum = new HashMap<>();

    /**
     * The start time of the host powered on.
     **/
    private Map<Integer, Double> powerOnHostStartTime = new HashMap<>();

    /**
     * The maximum number of hosts powered on.
     **/
    @Getter
    private int maxHostNum = 0;

    /**
     * The number of hosts powered on now.
     **/
    private int nowHostNum = 0;

    /**
     * The total power on time of the all hosts.
     **/
    @Getter
    private double allPowerOnTime = 0;

    /**
     * When an instance is assigned to this host,
     * this function is called to record the relevant information
     */
    public void hostAllocateInstance(int hostId, double clock) {
        if (!powerOnHostInstanceNum.containsKey(hostId)) {
            powerOnHostInstanceNum.put(hostId, 1);
            powerOnHostStartTime.put(hostId, clock);
            nowHostNum += 1;
            if (nowHostNum > maxHostNum) {
                maxHostNum = nowHostNum;
            }
        } else {
            powerOnHostInstanceNum.put(hostId, powerOnHostInstanceNum.get(hostId) + 1);
        }
    }

    /**
     * When an instance is released from this host,
     * this function is called to record the relevant information
     */
    public void hostReleaseInstance(int hostId, double clock) {
        if (powerOnHostInstanceNum.get(hostId) == 1) {
            powerOnHostInstanceNum.remove(hostId);
            allPowerOnTime += clock - powerOnHostStartTime.get(hostId);
            powerOnHostStartTime.remove(hostId);
            nowHostNum -= 1;
        } else {
            powerOnHostInstanceNum.put(hostId, powerOnHostInstanceNum.get(hostId) - 1);
        }
    }
}
