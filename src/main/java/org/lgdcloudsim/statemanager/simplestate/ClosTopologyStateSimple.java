package org.lgdcloudsim.statemanager.simplestate;

import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.network.ClosTopology;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.StatesManager;
import org.lgdcloudsim.util.Range;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClosTopologyStateSimple {
    Datacenter originalDatacenter;
    ClosTopology originalClosTopology;
    
    long availableCpuSum;
    long availableRamSum;
    long availableStorageSum;
    long availableBwSum;
    long availableGpuSum;
    long topologyScoreSum;
    long hostNum;
    long randomScore;

    /**
     * Construct a new ClosTopologyStateSimple.
     *
     */
    public ClosTopologyStateSimple(Datacenter datacenter, ClosTopology closTopology) {
        this.originalDatacenter = datacenter;
        this.originalClosTopology = closTopology;

        // this.availableGpuSum = datacenter.getStatesManager().
        Range range = closTopology.getRange();
        this.hostNum = range.getMax() - range.getMin() + 1;
        StatesManager statemenger = datacenter.getStatesManager();
        for (int i = range.getMin(); i <= range.getMax(); i++) {
            HostState hostState = statemenger.getActualHostState(i);
            this.availableCpuSum += hostState.getCpu();
            this.availableRamSum += hostState.getRam();
            this.availableStorageSum += hostState.getStorage();
            this.availableBwSum += hostState.getBw();
            this.availableGpuSum += hostState.getGpu();
        }

        this.topologyScoreSum = closTopology.getTopologyScore();
        this.randomScore = (long)(Math.random() * Long.MAX_VALUE);
    }
}
