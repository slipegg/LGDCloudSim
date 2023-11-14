package org.cpnsim.statemanager;

import lombok.Getter;

public class DetailedDcStateSimple {
    int[] hostStates;
    @Getter
    int hostNum;

    public DetailedDcStateSimple(int[] hostStates) {
        this.hostStates = hostStates.clone();//必须clone，因为后面会对其进行修改，且这个值也必须和原来的值保证独立
        hostNum = hostStates.length / 4;
    }

    public HostState getHostState(int hostId) {
        return new HostState(hostStates[hostId * HostState.STATE_NUM], hostStates[hostId * HostState.STATE_NUM + 1], hostStates[hostId * HostState.STATE_NUM + 2], hostStates[hostId * HostState.STATE_NUM + 3]);
    }
}
