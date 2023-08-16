package org.cpnsim.statemanager;

import java.util.List;

public class PredictionManagerSimple implements PredictionManager {
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
