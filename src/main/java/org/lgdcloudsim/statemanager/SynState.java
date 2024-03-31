package org.lgdcloudsim.statemanager;

import org.lgdcloudsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This SynState is used by the intra-scheduler for resource scheduling.
 * Before the intra-scheduler starts scheduling, it will get the SynState from the {@link StatesManager}.
 * The SynState contains the state of all hosts in the datacenter in the intra-scheduler's view.
 * The intra-scheduler will only use the SynState to schedule the instances.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface SynState {
    /**
     * Logger for this class.
     */
    Logger LOGGER = LoggerFactory.getLogger(SynState.class.getSimpleName());

    /**
     * Get the host state of the host with hostId in the intra-scheduler's view.
     *
     * @param hostId the id of the host.
     * @return the host state.
     */
    HostState getHostState(int hostId);

    /**
     * Judging whether this instance is suitable to be placed on the host with hostId according to SynState
     * @param hostId the id of the host.
     * @param instance the instance to be placed.
     * @return true if the instance is suitable to be placed on the host, otherwise false.
     */
    boolean isSuitable(int hostId, Instance instance);

    /**
     * Pretend that resources have been allocated on this host and modify the corresponding SysState
     * @param hostId the id of the host.
     * @param instance the instance to be placed.
     */
    void allocateTmpResource(int hostId, Instance instance);
}
