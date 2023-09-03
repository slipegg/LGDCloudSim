package org.cpnsim.statemanager;

import java.util.List;

/**
 * An interface to be implemented by each class that predicts the state of a host.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface PredictionManager {
    /**
     * Predict the state of a host.
     *
     * @param hostStateHistories An array of HostStateHistory
     * @return the predicted state of the host.the state includes 4 integers: cpu, ram, storage and bw.
     * */
    int[] predictHostState(List<HostStateHistory> hostStateHistories);
}
