package org.cpnsim.statemanager;

import lombok.Getter;
import org.cpnsim.request.Instance;

public class DetailedDcStateSimple {
    int[] hostStates;

    final HostCapacityManager hostCapacityManager;

    @Getter
    final int hostNum;
    @Getter
    long cpuAvailableSum;
    @Getter
    long ramAvailableSum;
    @Getter
    long storageAvailableSum;
    @Getter
    long bwAvailableSum;

    public DetailedDcStateSimple(int[] hostStates, HostCapacityManager hostCapacityManager, long cpuAvailableSum, long ramAvailableSum, long storageAvailableSum, long bwAvailableSum) {
        this.hostStates = hostStates.clone();//必须clone，因为后面会对其进行修改，且这个值也必须和原来的值保证独立
        this.hostCapacityManager = hostCapacityManager;
        hostNum = hostStates.length / 4;
        this.cpuAvailableSum = cpuAvailableSum;
        this.ramAvailableSum = ramAvailableSum;
        this.storageAvailableSum = storageAvailableSum;
        this.bwAvailableSum = bwAvailableSum;
    }

    public HostState getHostState(int hostId) {
        return new HostState(hostStates[hostId * HostState.STATE_NUM], hostStates[hostId * HostState.STATE_NUM + 1], hostStates[hostId * HostState.STATE_NUM + 2], hostStates[hostId * HostState.STATE_NUM + 3]);
    }

    public int[] getHostCapacity(int hostId) {
        return hostCapacityManager.getHostCapacity(hostId);
    }

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
