package org.scalecloudsim.statemanager;

public interface DelayState {
    int[] getHostState(int hostId);
    int[] getAllState();
}
