package org.lgdcloudsim.shadowresource.hostsrmapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.requestmapper.PartitionSRRequestMapper;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;
import org.lgdcloudsim.shadowresource.util.SRRequestScheduledRes;
import org.lgdcloudsim.statemanager.HostState;
import org.lgdcloudsim.statemanager.PartitionRangesManager;

public class HostSRMapperManager {
    public static final int NO_SCHEDULE = -1;
    private PartitionRangesManager partitionRangesManager;
    private Map<Integer, PartitionHostSRMapper> hostSRMapperMap;

    public HostSRMapperManager(PartitionRangesManager partitionRangesManager) {
        this.partitionRangesManager = partitionRangesManager;
        hostSRMapperMap = new HashMap<>();
    }

    public HostSRMapperManager setHostSRMapper(int partitionId, PartitionHostSRMapper partitionHostSRMapper) {
        hostSRMapperMap.put(partitionId, partitionHostSRMapper);
        return this;
    }

    public PartitionHostSRMapper getPartitionHostSRMapperSimple(int hostId){
        return hostSRMapperMap.get(hostId);
    }

    public int collectSR(int hostId, HostState hostState, int[] hostCapacity, Instance releasedInstance, double SRLife) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        PartitionHostSRMapper partitionHostSRMapperSimple = hostSRMapperMap.get(partitionId);
        HostSR hostSR = new HostSR(hostId, releasedInstance.getCpu(), releasedInstance.getRam(), SRLife,  hostState.getCpu(), hostState.getRam(), hostCapacity[0], hostCapacity[1]);
        partitionHostSRMapperSimple.receiveHostSR(hostSR);

        if (!partitionHostSRMapperSimple.isBusy()) {
            partitionHostSRMapperSimple.setBusy(true);
            return partitionId;
        }
        return NO_SCHEDULE;
    }

    public SRRequestScheduledRes scheduleForNewHostSR(int partitionId){
        PartitionHostSRMapper partitionHostSRMapperSimple = hostSRMapperMap.get(partitionId);
        
        double start = System.currentTimeMillis();
        List<SRRequest> scheduledRequests = partitionHostSRMapperSimple.scheduleForNewHostSR();
        double end = System.currentTimeMillis();
        return new SRRequestScheduledRes(partitionId, scheduledRequests, end - start);
    }

    public boolean isContinueSchedule(int partitionId){
        PartitionHostSRMapper partitionHostSRMapperSimple = hostSRMapperMap.get(partitionId);
        
        return partitionHostSRMapperSimple.isQueueEmpty();
    }

    public HostSRMapperManager stopSchedule(int partitionId){
        PartitionHostSRMapper partitionHostSRMapperSimple = hostSRMapperMap.get(partitionId);
        partitionHostSRMapperSimple.setBusy(false);
        return this;
    }
}
