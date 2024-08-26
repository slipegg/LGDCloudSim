package org.lgdcloudsim.shadowresource.filter;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.lifepredictor.LifePredictor;
import org.lgdcloudsim.shadowresource.partitionmanager.SRCoordinator;
import org.lgdcloudsim.shadowresource.partitionmanager.SRPartitionManager;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SRRequestFilterSimple implements SRRequestFilter {
    Random random;
    LifePredictor lifePredictor;
    @Setter
    SRCoordinator srCoordinator;

    @Getter
    private class SRRequestsAndNormalInstances {
        List<SRRequest> srRequests;
        List<Instance> normalInstances;

        public SRRequestsAndNormalInstances() {
            srRequests = new ArrayList<>();
            normalInstances = new ArrayList<>();
        }
    }

    @Override
    public SRRequestFilterRes filter(List<Instance> instances) {
        SRRequestsAndNormalInstances srRequestsAndNormalInstances = filterToSRRequests(instances);

        Map<Integer, List<SRRequest>> distributedSRMap = distributeSRRequests(srRequestsAndNormalInstances.getSrRequests());

        return new SRRequestFilterRes(srRequestsAndNormalInstances.getNormalInstances(), distributedSRMap);
    }

    private SRRequestsAndNormalInstances filterToSRRequests(List<Instance> instances) {
        SRRequestsAndNormalInstances srRequestsAndNormalInstances = new SRRequestsAndNormalInstances();
        Map<Integer, SRPartitionManager> partitionManagerMap = srCoordinator.getPartitionManagerMap();

        long remainedCpu = partitionManagerMap.values().stream().mapToLong(partitionManager -> partitionManager.getTotalHostSRCpu() - partitionManager.getTotalSRRequestedCpu()).sum();

        // if (remainedCpu > 0) {
        //     for (Instance instance : instances) {
        //         SRRequest srRequest = isChangeToSRRequest(instance);
        //         if (srRequest != null) {
        //             remainedCpu -= srRequest.getInstance().getCpu();
        //             srRequestsAndNormalInstances.getSrRequests().add(srRequest);
        //         } else {
        //             srRequestsAndNormalInstances.getNormalInstances().add(instance);
        //         }
        //     }
        // } 
        int filteredIndex = 0;
        for (; filteredIndex < instances.size() && remainedCpu > 0; filteredIndex++) {
            Instance instance = instances.get(filteredIndex);
            SRRequest srRequest = isChangeToSRRequest(instance);
            if (srRequest != null) {
                remainedCpu -= srRequest.getInstance().getCpu();
                srRequestsAndNormalInstances.getSrRequests().add(srRequest);
            } else {
                srRequestsAndNormalInstances.getNormalInstances().add(instance);
            }
        }
        if (filteredIndex < instances.size()) {
            srRequestsAndNormalInstances.getNormalInstances().addAll(instances.subList(filteredIndex, instances.size()));
        }

        return srRequestsAndNormalInstances;
    }

    @Getter
    private class PartitionItem{
        int partitionId;
        long remainedCpu;

        public PartitionItem(int partitionId, long remainedCpu) {
            this.partitionId = partitionId;
            this.remainedCpu = remainedCpu;
        }
    }


    private Map<Integer, List<SRRequest>> distributeSRRequests(List<SRRequest> newSRRequests) {
        Map<Integer, List<SRRequest>> scheduleRequestMap = new HashMap<>();

        List<PartitionItem> partitionItems = new ArrayList<>();
        for (int partitionId : srCoordinator.getPartitionManagerMap().keySet()) {
            SRPartitionManager partitionManager = srCoordinator.getPartitionManagerMap().get(partitionId);
            long remainedCpu = partitionManager.getTotalHostSRCpu() - partitionManager.getTotalSRRequestedCpu();
            if (remainedCpu > 0){
                partitionItems.add(new PartitionItem(partitionId, remainedCpu));
            }
        }

        Collections.sort(partitionItems, new Comparator<PartitionItem>() {
            @Override
            public int compare(PartitionItem o1, PartitionItem o2) {
                return o1.remainedCpu > o2.remainedCpu ? 1 : -1;
            }
        });

        long totalCpu = newSRRequests.stream().mapToLong(request -> request.getInstance().getCpu()).sum();
        long[] remainedCpuArray = partitionItems.stream().mapToLong(item -> item.getRemainedCpu()).toArray();

        int[] distributedPartitionIds = new int[newSRRequests.size()];
        for (int i = 0; i < newSRRequests.size(); i++) {
            distributedPartitionIds[i] = distributeCpuResources(remainedCpuArray, totalCpu);
        }

        for (int i = 0; i < newSRRequests.size(); i++) {
            scheduleRequestMap.computeIfAbsent(distributedPartitionIds[i], k -> new ArrayList<>()).add(newSRRequests.get(i));
        }

        return scheduleRequestMap;

    }

    private int distributeCpuResources(long[] resources, long newResources) {
        if(newResources <= 0) {
            return 0;
        }
        long consumeResource = 0;
        int currentLevelId = 0;
        int nextLevelId = getNextLevelId(currentLevelId, resources);
        while (nextLevelId < resources.length) {
            long diff = resources[nextLevelId] - resources[currentLevelId];
            consumeResource += diff * (nextLevelId);
            if (newResources < consumeResource) {
                break;
            }
            currentLevelId = nextLevelId;
            nextLevelId = getNextLevelId(currentLevelId, resources);
        }
        return currentLevelId;
    }

    private int getNextLevelId(int currentLevelId, long[] resources) {
        int nextLevelId;
        for(nextLevelId = currentLevelId; nextLevelId < resources.length; nextLevelId++) {
            if (resources[nextLevelId] > resources[currentLevelId]) {
                return nextLevelId;
            }
        }
        return nextLevelId;
    }

    private SRRequest isChangeToSRRequest(Instance instance) {
        double predictLife = lifePredictor.predictLife(instance);
        if(random.nextDouble()<1){
            return new SRRequest(instance, predictLife);
        }else{
            return null;
        }
    }

    public SRRequestFilterSimple(LifePredictor lifePredictor) {
        random = new Random();
        this.lifePredictor = lifePredictor;
    }
}
