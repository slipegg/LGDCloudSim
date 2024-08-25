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

public class SRCoordinator implements Nameable {
    /**
     * the Logger.
     **/
    private Logger LOGGER = LoggerFactory.getLogger(SRCoordinator.class.getSimpleName());

    private int id;

    private Simulation simulation;
    
    @Getter
    private SRRequestFilter srRequestFilter;

    private PartitionRangesManager partitionRangesManager;

    Map<Integer, SRPartitionManager> partitionManagerMap;

    public SRCoordinator(SRRequestFilter srRequestFilter, PartitionRangesManager partitionRangesManager, Map<Integer, SRPartitionManager> partitionManagerMap, Simulation simulation) {
        this.simulation = simulation;
        this.id = 0;
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

    public List<Integer> receiveSRRequests(List<SRRequest> srRequests) {
        List<Integer> needStartScheduledPartitionIds = new ArrayList<>();
        int size = srRequests.size();
        if (size>0){
            return needStartScheduledPartitionIds;
        }

        Map<Integer, List<SRRequest>> distributedScheduleRequestMap = distribuedSRRequests(srRequests);

        for (Map.Entry<Integer, List<SRRequest>> entry : distributedScheduleRequestMap.entrySet()) {
            int partitionId = entry.getKey();
            List<SRRequest> partitionSRRequests = entry.getValue();

            SRPartitionManager partitionManager = partitionManagerMap.get(partitionId);
            if (!partitionSRRequests.isEmpty()) {
                partitionManager.addToQueue(partitionSRRequests);
                if (!partitionManager.isSRRequestScheduleBusy()) {
                    partitionManager.setSRRequestScheduleBusy(true);
                    needStartScheduledPartitionIds.add(partitionId);
                }
            }
        }

        return needStartScheduledPartitionIds;
    }

    @Getter
    private class partitionItem{
        int partitionId;
        long remainedCpu;

        public partitionItem(int partitionId, long remainedCpu) {
            this.partitionId = partitionId;
            this.remainedCpu = remainedCpu;
        }
    }

    private Map<Integer, List<SRRequest>> distribuedSRRequests(List<SRRequest> newSRRequests){
        Map<Integer, List<SRRequest>> scheduleRequestMap = new HashMap<>();

        List<partitionItem> partitionItems = new ArrayList<>();
        for (int partitionId : partitionManagerMap.keySet()) {
            SRPartitionManager partitionManager = partitionManagerMap.get(partitionId);
            long remainedCpu = partitionManager.getTotalHostSRCpu() - partitionManager.getTotalSRRequestedCpu();
            partitionItems.add(new partitionItem(partitionId, remainedCpu));
        }

        Collections.sort(partitionItems, new Comparator<partitionItem>() {
            @Override
            public int compare(partitionItem o1, partitionItem o2) {
                return o1.remainedCpu > o2.remainedCpu ? 1 : -1;
            }
        });

        int[] remainedCpuArray = new int[partitionItems.size()];
        for (int i = 0; i < partitionItems.size(); i++) {
            remainedCpuArray[i] = (int) partitionItems.get(i).remainedCpu;
        }
    
        int totalCpu = 0;
        for (SRRequest request : newSRRequests) {
            totalCpu += request.getInstance().getCpu();
        }
    
        int[] distributedResources = distributeCpuResources(remainedCpuArray, totalCpu);
    
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < partitionItems.size(); i++) {
            int partitionId = partitionItems.get(i).partitionId;
    
            // 分配请求到每个分区
            while (endIndex < newSRRequests.size() && (distributedResources[i] > 0 && i != partitionItems.size() - 1)) {
                SRRequest request = newSRRequests.get(endIndex);
                distributedResources[i] -= request.getInstance().getCpu();
                endIndex++;
            }

            scheduleRequestMap.put(partitionId, newSRRequests.subList(startIndex, endIndex+1));
            endIndex++;
            startIndex = endIndex;
        }

        return scheduleRequestMap;
    }

    private int[] distributeCpuResources(int[] resources, int newResources) {
        int[] result = new int[resources.length];
        if(newResources <= 0) {
            return result;
        }
        int consumeResource = 0;
        int currentLevelId = 0;
        int nextLevelId = getNextLevelId(currentLevelId, resources);
        while (nextLevelId < resources.length) {
            int diff = resources[nextLevelId] - resources[currentLevelId];
            consumeResource += diff * (nextLevelId);
            if (newResources < consumeResource) {
                break;
            }
            currentLevelId = nextLevelId;
            nextLevelId = getNextLevelId(currentLevelId, resources);
        }
        if (nextLevelId >= resources.length) {
            // 资源足够平衡
            int diff = newResources - consumeResource;
            int average = diff / resources.length;
            int remainder = diff % resources.length;
            for (int i = 0; i < resources.length; i++) {
                result[i] = average + resources[resources.length - 1] - resources[i];
                if (i < remainder) {
                    result[i]++;
                }
            }
        } else {
            // 资源不够平衡
            consumeResource = 0;
            for(int i = 0; i < currentLevelId; i++) {
                consumeResource += resources[currentLevelId] - resources[i];
            }
            int diff = newResources - consumeResource;
            int average = diff / (nextLevelId);
            int remainder = diff % (nextLevelId);
            for (int i = 0; i < nextLevelId; i++) {
                result[i] = average + resources[currentLevelId] - resources[i];
                if (i < remainder) {
                    result[i]++;
                }
            }
        }

        return result;
    }

    private int getNextLevelId(int currentLevelId, int[] resources) {
        int nextLevelId ;
        for(nextLevelId = currentLevelId; nextLevelId < resources.length; nextLevelId++) {
            if (resources[nextLevelId] > resources[currentLevelId]) {
                return nextLevelId;
            }
        }
        return nextLevelId;
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
