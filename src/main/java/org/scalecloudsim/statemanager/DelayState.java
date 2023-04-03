package org.scalecloudsim.statemanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DelayState {
    public Logger LOGGER = LoggerFactory.getLogger(DelayState.class.getSimpleName());

    int[] getHostState(int hostId);

    int[] getAllState();
}
