package org.scalecloudsim.datacenter;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class DatacenterPowerOnRecord {
    @Getter
    Map<Integer, Integer> powerOnHostInstanceNum = new HashMap<>();
    Map<Integer, Double> powerOnHostStartTime = new HashMap<>();
    @Getter
    int maxHostNum = 0;
    int nowHostNum = 0;
    @Getter
    double allPowerOnTime = 0;

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
