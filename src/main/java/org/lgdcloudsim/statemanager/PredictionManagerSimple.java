package org.lgdcloudsim.statemanager;

import java.util.List;

/**
 * A class to predict the state of a host.
 * The prediction method is simple: the predicted state is the average of the historical state.
 * This class implements the interface {@link PredictionManager}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class PredictionManagerSimple implements PredictionManager {
    /**
     * Predict the state of a host.
     * The prediction method is simple: the predicted state is the average of the historical state.
     *
     * @param hostStateHistories An array of HostStateHistory
     * @return the predicted state of the host. The state includes 4 integers: cpu, ram, storage and bw.
     * */
    @Override
    public int[] predictHostState(List<HostStateHistory> hostStateHistories) {
        long cpuSum = 0;
        long ramSum = 0;
        long storageSum = 0;
        long bwSum = 0;
        for (HostStateHistory hostStateHistory : hostStateHistories) {
            cpuSum += hostStateHistory.getCpu();
            ramSum += hostStateHistory.getRam();
            storageSum += hostStateHistory.getStorage();
            bwSum += hostStateHistory.getBw();
        }
        return new int[]{(int) (cpuSum / hostStateHistories.size()), (int) (ramSum / hostStateHistories.size()), (int) (storageSum / hostStateHistories.size()), (int) (bwSum / hostStateHistories.size())};
    }
}
