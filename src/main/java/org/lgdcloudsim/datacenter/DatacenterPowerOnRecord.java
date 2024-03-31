package org.lgdcloudsim.datacenter;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class to record the power on hosts information of the datacenter.
 * It will record the number of instances running on each powered on host,
 * the start time of the host powered on, the maximum number of hosts powered on during the simulation,
 * the number of hosts powered on now, and the total power on time of the all hosts.
 * It can be used to analyze the rental cost of the datacenter.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class DatacenterPowerOnRecord {
    /**
     * Record the number of instances running on each powered on host
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
    @Getter
    private int nowPowerOnHostNum = 0;

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
            nowPowerOnHostNum += 1;
            if (nowPowerOnHostNum > maxHostNum) {
                maxHostNum = nowPowerOnHostNum;
            }
        } else {
            powerOnHostInstanceNum.put(hostId, powerOnHostInstanceNum.get(hostId) + 1);
        }
    }

    /**
     * When an instance is released from this host,
     * this function is called to record the relevant information
     * TODO: Sometimes it happens that powerOnHostInstanceNum does not contain the host ID to be released and needs to be checked.
     */
    public void hostReleaseInstance(int hostId, double clock) {
        if (powerOnHostInstanceNum.get(hostId) == 1) {
            powerOnHostInstanceNum.remove(hostId);
            allPowerOnTime += clock - powerOnHostStartTime.get(hostId);
            powerOnHostStartTime.remove(hostId);
            nowPowerOnHostNum -= 1;
        } else {
            powerOnHostInstanceNum.put(hostId, powerOnHostInstanceNum.get(hostId) - 1);
        }
    }
}
