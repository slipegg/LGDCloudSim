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
    List<HostState> simpleHostStates;

    public SimpleStateEasyObject(int hostNum, long cpuAvailableSum, long ramAvailableSum, long storageAvailableSum, long bwAvailableSum, List<HostState> simpleHostStates) {
        this.hostNum = hostNum;
        this.cpuAvailableSum = cpuAvailableSum;
        this.ramAvailableSum = ramAvailableSum;
        this.storageAvailableSum = storageAvailableSum;
        this.bwAvailableSum = bwAvailableSum;
        this.simpleHostStates = simpleHostStates;
    }

    public int getAvgSimpleHostStateCpu() {
        return (int) (simpleHostStates.stream()
                .mapToLong(HostState::getCpu)
                .average()
                .orElse(0));
    }

    public int getAvgSimpleHostStateRam() {
        return (int) (simpleHostStates.stream()
                .mapToLong(HostState::getRam)
                .average()
                .orElse(0));
    }

    public int getAvgSimpleHostStateStorage() {
        return (int) (simpleHostStates.stream()
                .mapToLong(HostState::getStorage)
                .average()
                .orElse(0));
    }

    public int getAvgSimpleHostStateBw() {
        return (int) (simpleHostStates.stream()
                .mapToLong(HostState::getBw)
                .average()
                .orElse(0));
    }
}
