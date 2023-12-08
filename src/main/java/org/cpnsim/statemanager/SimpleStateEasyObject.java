package org.cpnsim.statemanager;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SimpleStateEasyObject {
    int hostNum;
    long cpuAvailableSum;
    long ramAvailableSum;
    long storageAvailableSum;
    long bwAvailableSum;
    long cpuCapacitySum;
    long ramCapacitySum;
    long storageCapacitySum;
    long bwCapacitySum;

    public SimpleStateEasyObject(int hostNum, long cpuAvailableSum, long ramAvailableSum, long storageAvailableSum, long bwAvailableSum, long cpuCapacitySum, long ramCapacitySum, long storageCapacitySum, long bwCapacitySum) {
        this.hostNum = hostNum;
        this.cpuAvailableSum = cpuAvailableSum;
        this.ramAvailableSum = ramAvailableSum;
        this.storageAvailableSum = storageAvailableSum;
        this.bwAvailableSum = bwAvailableSum;
        this.cpuCapacitySum = cpuCapacitySum;
        this.ramCapacitySum = ramCapacitySum;
        this.storageCapacitySum = storageCapacitySum;
    }

    public void allocateResource(long cpu, long ram, long storage, long bw) {
        cpuAvailableSum -= cpu;
        ramAvailableSum -= ram;
        storageAvailableSum -= storage;
        bwAvailableSum -= bw;
    }
}
