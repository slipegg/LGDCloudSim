package org.lgdcloudsim.shadowresource.requestmapper;

import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.hostsrmapper.HostSR;
import org.lgdcloudsim.statemanager.PartitionRangesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MapCoordinator implements Nameable {
    /**
     * the Logger.
     **/
    private Logger LOGGER = LoggerFactory.getLogger(PartitionSRRequestMapper.class.getSimpleName());

    private int id;

    private Simulation simulation;

    private PartitionRangesManager partitionRangesManager;

    Map<Integer, PartitionSRRequestMapper> partitionSRRequestMappers;

    GlobalSRRequestMapper globalSRRequestMapper;

    public MapCoordinator(PartitionRangesManager partitionRangesManager, Simulation simulation, int id) {
        this.simulation = simulation;
        this.id = id;
        this.partitionRangesManager = partitionRangesManager;
        int[] partitionIds = partitionRangesManager.getPartitionIds();
        for (int partitionId : partitionIds) {
            partitionSRRequestMappers.put(partitionId, new PartitionSRRequestMapper(this, simulation, partitionId));
        }
        globalSRRequestMapper = new GlobalSRRequestMapper(simulation);
    }

    public List<Integer> receiveSRRequest(List<SRRequest> srRequests) {
        List<Integer> scheduleRequestMap = new ArrayList<>();
        List<Integer> distributedScheduleRequestMapIds = distributeSRRequestMap(srRequests);
        if (distributedScheduleRequestMapIds.isEmpty()) {
            globalSRRequestMapper.addRequest(srRequests);
        }

        return distributedScheduleRequestMapIds;
    }

    public List<Integer> distributeSRRequestMap(List<SRRequest> srRequests) {
        List<PartitionSRRequestMapper> emptyScheduleRequestMapList = new ArrayList<>();
        for (PartitionSRRequestMapper partitionSRRequestMapper : partitionSRRequestMappers.values()) {
            if (partitionSRRequestMapper.isEmpty()) {
                emptyScheduleRequestMapList.add(partitionSRRequestMapper);
            }
        }

        int emptySize = emptyScheduleRequestMapList.size();

        List<Integer> scheduleRequestMapList = new ArrayList<>();
        int totalSize = srRequests.size();
        int basePartitionSize = totalSize / emptySize; // 每个分区的基本大小
        int largerPartitions = totalSize % emptySize; // 较大分区的数量

        int currentIndex = 0;
        for (int i = 0; i < emptySize; i++) {
            int partitionSize = basePartitionSize + (i < largerPartitions ? 1 : 0); // 较大分区多一个元素
            int end = Math.min(currentIndex + partitionSize, totalSize);
            emptyScheduleRequestMapList.get(i).addRequests(srRequests.subList(currentIndex, end));
            scheduleRequestMapList.add(emptyScheduleRequestMapList.get(i).getId());
            currentIndex = end;
        }

        return scheduleRequestMapList;
    }

    public PartitionSRRequestMapper getPartitionSRRequestMapperByPartitionId(int partitionId) {
        return partitionSRRequestMappers.get(partitionId);
    }

    public PartitionSRRequestMapper getPartitionSRRequestMapper(int hostId) {
        int partitionId = partitionRangesManager.getPartitionId(hostId);
        return partitionSRRequestMappers.get(partitionId);
    }

    public void recordMissing(int partition, HostSR missingHostSR) {
        // TODO 如何处理缺失需要多考虑，目前是简单处理了
        List<SRRequest> srRequests = globalSRRequestMapper.pop(10);
        if (srRequests.isEmpty()) {
            balancePartitionSRRequestMappers();
        } else {
            partitionSRRequestMappers.get(partition).addRequests(srRequests);
        }
    }

    private void balancePartitionSRRequestMappers() {
        PartitionSRRequestMapper maxCpuPartitionSRRequestMapper = partitionSRRequestMappers.values().stream()
                .max(Comparator.comparingLong(PartitionSRRequestMapper::getSRRequestCpuTotal))
                .orElseThrow(() -> new NoSuchElementException("No elements in the map"));
        PartitionSRRequestMapper minCpuPartitionSRRequestMapper = partitionSRRequestMappers.values().stream()
                .min(Comparator.comparingLong(PartitionSRRequestMapper::getSRRequestCpuTotal))
                .orElseThrow(() -> new NoSuchElementException("No elements in the map"));
        long cpuDiff = maxCpuPartitionSRRequestMapper.getSRRequestCpuTotal() - minCpuPartitionSRRequestMapper.getSRRequestCpuTotal();
        long memoryDiff = maxCpuPartitionSRRequestMapper.getSRRequestMemoryTotal() - minCpuPartitionSRRequestMapper.getSRRequestMemoryTotal();
        if (cpuDiff / 2 > 0 && memoryDiff / 2 > 0) {
            List<SRRequest> srRequests = maxCpuPartitionSRRequestMapper.popRandom(cpuDiff / 2, memoryDiff / 2);
            minCpuPartitionSRRequestMapper.addRequests(srRequests);
        }
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
