package org.cpnsim.statemanager;

import org.cpnsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/*
 * This SynState is used by the {@link InnerScheduler} for resource scheduling, and each InnerScheduler has its own SynState
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface SynState {
    Logger LOGGER = LoggerFactory.getLogger(SynState.class.getSimpleName());

    /** Judging whether this instance is suitable to be placed on the host with hostId according to SynState **/
    boolean isSuitable(int hostId, Instance instance);

    /** Pretend that resources have been allocated on this host and modify the corresponding SysState **/
    void allocateTmpResource(int hostId, Instance instance);
}
