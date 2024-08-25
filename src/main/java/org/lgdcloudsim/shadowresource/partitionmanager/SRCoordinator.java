package org.lgdcloudsim.shadowresource.partitionmanager;

import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.shadowresource.filter.SRRequestFilter;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;
import org.lgdcloudsim.statemanager.PartitionRangesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

import java.util.*;

public class SRCoordinator {    
    @Getter
    private SRRequestFilter srRequestFilter;

    private PartitionRangesManager partitionRangesManager;

    @Getter
    Map<Integer, SRPartitionManager> partitionManagerMap;

    public SRCoordinator(SRRequestFilter srRequestFilter, PartitionRangesManager partitionRangesManager, Map<Integer, SRPartitionManager> partitionManagerMap) {
        this.srRequestFilter = srRequestFilter;
        this.partitionRangesManager = partitionRangesManager;
        this.partitionManagerMap = partitionManagerMap;
    }
    
    public SRPartitionManager getPartitionManagerByPartitionId(int partitionId) {
        return partitionManagerMap.get(partitionId);
    }

    public SRPartitionManager getPartitionManagerByHostId(int hostId) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        return partitionManagerMap.get(partitionId);
    }
}
