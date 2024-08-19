package org.lgdcloudsim.shadowresource.requestmapper;

import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;
import org.lgdcloudsim.shadowresource.hostsrmapper.PartitionHostSRMapper;
import org.lgdcloudsim.shadowresource.util.SRRequestScheduledRes;
import org.lgdcloudsim.shadowresource.util.SRScheduleRes;
import org.lgdcloudsim.statemanager.PartitionRangesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SRRequestMapCoordinator implements Nameable {
    /**
     * the Logger.
     **/
    private Logger LOGGER = LoggerFactory.getLogger(PartitionSRRequestMapper.class.getSimpleName());

    private int id;

    private Simulation simulation;

    private PartitionRangesManager partitionRangesManager;

    Map<Integer, PartitionSRRequestMapper> partitionSRRequestMappers;

    GlobalSRRequestMapper globalSRRequestMapper;

    private double globalRate = 0.2;

    public SRRequestMapCoordinator(PartitionRangesManager partitionRangesManager, Simulation simulation, int id) {
        this.simulation = simulation;
        this.id = id;
        this.partitionRangesManager = partitionRangesManager;
        int[] partitionIds = partitionRangesManager.getPartitionIds();
        for (int partitionId : partitionIds) {
            partitionSRRequestMappers.put(partitionId, new PartitionSRRequestMapper(this, simulation, partitionId));
        }
        globalSRRequestMapper = new GlobalSRRequestMapper(simulation);
    }

    public List<Integer> receiveSRRequests(List<SRRequest> srRequests) {
        List<Integer> needStartScheduledPartitionIds = new ArrayList<>();
        int size = srRequests.size();
        if (size>0){
            return needStartScheduledPartitionIds;
        }
        int globalSize = getGlobalMapSizeFromReceived(srRequests);
        if (globalSize > 0) {
            List<SRRequest> globalList = srRequests.subList(0, globalSize);
            globalSRRequestMapper.addRequest(globalList);
        }

        List<SRRequest> partitionList = srRequests.subList(globalSize, size);
        Map<Integer, List<SRRequest>> distributedScheduleRequestMap = distribuedSRRequestsEvenly(partitionList);

        for (Map.Entry<Integer, List<SRRequest>> entry : distributedScheduleRequestMap.entrySet()) {
            int partitionId = entry.getKey();
            List<SRRequest> partitionSRRequests = entry.getValue();

            PartitionSRRequestMapper partitionSRRequestMapper = partitionSRRequestMappers.get(partitionId);
            if (!partitionSRRequests.isEmpty()) {
                partitionSRRequestMapper.addSRRequestToQueue(partitionSRRequests);
                if (!partitionSRRequestMapper.isBusy()) {
                    partitionSRRequestMapper.setBusy(true);
                    needStartScheduledPartitionIds.add(partitionId);
                }
            }
        }

        return needStartScheduledPartitionIds;
    }

    private Map<Integer, List<SRRequest>> distribuedSRRequestsEvenly(List<SRRequest> srRequests){
        int partitionMapperSize = partitionSRRequestMappers.size();
        Map<Integer, List<SRRequest>> scheduleRequestMap = new HashMap<>();
        int totalSize = srRequests.size();
        int basePartitionSize = totalSize / partitionMapperSize; // 每个分区的基本大小
        int largerPartitions = totalSize % partitionMapperSize; // 较大分区的数量

        int currentIndex = 0;
        for (int i = 0; i < partitionMapperSize; i++) {
            int partitionSize = basePartitionSize + (i < largerPartitions ? 1 : 0); // 较大分区多一个元素
            int end = Math.min(currentIndex + partitionSize, totalSize);
            scheduleRequestMap.put(partitionSRRequestMappers.get(i).getId(), srRequests.subList(currentIndex, end));
            currentIndex = end;
        }

        return scheduleRequestMap;

    }

    private int getGlobalMapSizeFromReceived(List<SRRequest> srRequests){
        return (int)(srRequests.size() * globalRate);
    }

    // public Map<Integer, List<SRRequest>> receiveSRRequest(List<SRRequest> srRequests) {
    //     if (srRequests.isEmpty()) {
    //         return new HashMap<>();
    //     }  
    //     Map<Integer, List<SRRequest>> distributedScheduleRequestMap = distributeSRRequestMap(srRequests);
    //     if (distributedScheduleRequestMap.isEmpty()) {
    //         globalSRRequestMapper.addRequest(srRequests);
    //     }
    //     return distributedScheduleRequestMap;
    // }

    public List<SRRequestScheduledRes> scheduleForNewSRRequests(List<Integer> partitionIds){
        List<SRRequestScheduledRes> srScheduleResList = new ArrayList<>();
        for (int partitionId : partitionIds) {
            srScheduleResList.add(scheduleForNewSRRequest(partitionId));
        }
        return srScheduleResList;
    }

    public SRRequestScheduledRes scheduleForNewSRRequest(int partitionId){
        PartitionSRRequestMapper partitionSRRequestMapper = partitionSRRequestMappers.get(partitionId);
        
        double start = System.currentTimeMillis();
        List<SRRequest> scheduledRequests = partitionSRRequestMapper.scheduleForNewSRRequests();
        double end = System.currentTimeMillis();
        return new SRRequestScheduledRes(partitionId,scheduledRequests, end - start);
    }

    public Map<Integer, List<SRRequest>> distributeSRRequestMap(List<SRRequest> srRequests) {
        List<PartitionSRRequestMapper> emptyScheduleRequestMapList = new ArrayList<>();
        for (PartitionSRRequestMapper partitionSRRequestMapper : partitionSRRequestMappers.values()) {
            if (partitionSRRequestMapper.isEmpty()) {
                emptyScheduleRequestMapList.add(partitionSRRequestMapper);
            }
        }

        int emptySize = emptyScheduleRequestMapList.size();

        Map<Integer, List<SRRequest>> scheduleRequestMap = new HashMap<>();
        int totalSize = srRequests.size();
        int basePartitionSize = totalSize / emptySize; // 每个分区的基本大小
        int largerPartitions = totalSize % emptySize; // 较大分区的数量

        int currentIndex = 0;
        for (int i = 0; i < emptySize; i++) {
            int partitionSize = basePartitionSize + (i < largerPartitions ? 1 : 0); // 较大分区多一个元素
            int end = Math.min(currentIndex + partitionSize, totalSize);
            scheduleRequestMap.put(emptyScheduleRequestMapList.get(i).getId(), srRequests.subList(currentIndex, end));
            currentIndex = end;
        }

        return scheduleRequestMap;
    }

    public PartitionSRRequestMapper getPartitionSRRequestMapperByPartitionId(int partitionId) {
        return partitionSRRequestMappers.get(partitionId);
    }

    public PartitionSRRequestMapper getPartitionSRRequestMapper(int hostId) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        return partitionSRRequestMappers.get(partitionId);
    }

    // private void balancePartitionSRRequestMappers() {
    //     PartitionSRRequestMapper maxCpuPartitionSRRequestMapper = partitionSRRequestMappers.values().stream()
    //             .max(Comparator.comparingLong(PartitionSRRequestMapper::getSRRequestCpuTotal))
    //             .orElseThrow(() -> new NoSuchElementException("No elements in the map"));
    //     PartitionSRRequestMapper minCpuPartitionSRRequestMapper = partitionSRRequestMappers.values().stream()
    //             .min(Comparator.comparingLong(PartitionSRRequestMapper::getSRRequestCpuTotal))
    //             .orElseThrow(() -> new NoSuchElementException("No elements in the map"));
    //     long cpuDiff = maxCpuPartitionSRRequestMapper.getSRRequestCpuTotal() - minCpuPartitionSRRequestMapper.getSRRequestCpuTotal();
    //     long memoryDiff = maxCpuPartitionSRRequestMapper.getSRRequestMemoryTotal() - minCpuPartitionSRRequestMapper.getSRRequestMemoryTotal();
    //     if (cpuDiff / 2 > 0 && memoryDiff / 2 > 0) {
    //         List<SRRequest> srRequests = maxCpuPartitionSRRequestMapper.popRandom(cpuDiff / 2, memoryDiff / 2);
    //         minCpuPartitionSRRequestMapper.addSRRequest(srRequests);
    //     }
    // }

    public boolean isContinueSchedule(int partitionId){
        PartitionSRRequestMapper partitionSRRequestMapper = partitionSRRequestMappers.get(partitionId);
        return partitionSRRequestMapper.isQueueEmpty();
    }

    public SRRequestMapCoordinator stopSchedule(int partitionId){
        PartitionSRRequestMapper partitionSRRequestMapper = partitionSRRequestMappers.get(partitionId);
        partitionSRRequestMapper.setBusy(false);
        return this;
    }

    public boolean balanceFromGlobalToPartitionMapper(int partitionId){
        int size = globalSRRequestMapper.getSize()
    }

    public int getAllSize() {
        return partitionSRRequestMappers.values().stream().mapToInt(PartitionSRRequestMapper::getSize).sum()+globalSRRequestMapper.getSize();
    }

    public long getSRRequestCpuTotal() {
        return partitionSRRequestMappers.values().stream().mapToLong(PartitionSRRequestMapper::getSRRequestCpuTotal).sum()+globalSRRequestMapper.getSRRequestCpuTotal();
    }

    public long getSRRequestMemoryTotal() {
        return partitionSRRequestMappers.values().stream().mapToLong(PartitionSRRequestMapper::getSRRequestMemoryTotal).sum()+globalSRRequestMapper.getSRRequestMemoryTotal();
    }
    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return "MapCoordinator";
    }
}
