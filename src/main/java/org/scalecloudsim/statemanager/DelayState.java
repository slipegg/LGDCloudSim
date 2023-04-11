package org.scalecloudsim.statemanager;

import org.scalecloudsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DelayState {
    Logger LOGGER = LoggerFactory.getLogger(DelayState.class.getSimpleName());

    int[] getHostState(int hostId);

    int[] getAllState();

    boolean isSuitable(int hostId, Instance instance);

    //假装分配了资源，每次分配后都会修改对应的状态
    void allocateTmpResource(int hostId, Instance instance);
}
