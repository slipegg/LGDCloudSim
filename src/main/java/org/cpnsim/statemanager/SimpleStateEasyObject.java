package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleStateEasyObject {
    long cpuAvailableSum;
    long ramAvailableSum;
    long storageAvailableSum;
    long bwAvailableSum;

    public SimpleStateEasyObject(long cpuAvailableSum, long ramAvailableSum, long storageAvailableSum, long bwAvailableSum) {
        this.cpuAvailableSum = cpuAvailableSum;
        this.ramAvailableSum = ramAvailableSum;
        this.storageAvailableSum = storageAvailableSum;
        this.bwAvailableSum = bwAvailableSum;
    }
}
