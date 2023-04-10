package org.scalecloudsim.statemanager;

import org.scalecloudsim.Instances.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DelayState {
    public Logger LOGGER = LoggerFactory.getLogger(DelayState.class.getSimpleName());

    int[] getHostState(int hostId);

    int[] getAllState();

    boolean isSuitable(int hostId, Instance instance);

    void allocateTmpResource(int hostId, Instance instance);
}
