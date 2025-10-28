package org.lgdcloudsim.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.network.ClosTopology;
import org.lgdcloudsim.network.ClosTopologyItem;
import org.lgdcloudsim.network.ClosTopologyManager;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceTopology;
import org.lgdcloudsim.request.TrainingStrategy;

public class StrategyUtils {
    // static int calculateDPSpread(InstanceGroup instanceGroup, ClosTopologyManager closTopologyManager) {
    //     int spread = 0;
    //     TrainingStrategy trainingStrategy = instanceGroup.getTrainingStrategy();
    //     if (trainingStrategy == null) {
    //         return spread;
    //     }
        
    //     int dpDim = trainingStrategy.getDPDim();
    //     int ppDim = trainingStrategy.getPPDim();
    //     for(int i = 0; i < dpDim; i++) {
    //         HashSet<String> pswSet = new HashSet<>();
    //         List<Instance> instanceList = instanceGroup.getInstances();
    //         for(int j = 0; j < ppDim; j++) {
    //             Instance instance = instanceList.get(i * ppDim + j);
    //             int hostId = instance.getHost();
    //             ClosTopologyItem closItem = closTopologyManager.getClosTopologyItemByHostId(hostId);
    //             pswSet.add(closItem.getPSW());
    //         }
    //         if (pswSet.size() > spread) {
    //             spread = pswSet.size();
    //         }
    //     }
        
    //     return spread;
    // }

    // static int calculatePPSpread(InstanceGroup instanceGroup, ClosTopologyManager closTopologyManager) {
    //     int spread = 0;
    //     TrainingStrategy trainingStrategy = instanceGroup.getTrainingStrategy();
    //     if (trainingStrategy == null) {
    //         return spread;
    //     }
        
    //     int dpDim = trainingStrategy.getDPDim();
    //     int ppDim = trainingStrategy.getPPDim();
    //     for(int i = 0; i < ppDim; i++) {
    //         HashSet<String> pswSet = new HashSet<>();
    //         List<Instance> instanceList = instanceGroup.getInstances();
    //         for(int j = 0; j < dpDim; j++) {
    //             Instance instance = instanceList.get(j * ppDim + i);
    //             int hostId = instance.getHost();
    //             ClosTopologyItem closItem = closTopologyManager.getClosTopologyItemByHostId(hostId);
    //             pswSet.add(closItem.getPSW());
    //         }
    //         if (pswSet.size() > spread) {
    //             spread = pswSet.size();
    //         }
    //     }
    //     return spread;    
    // }
    public static Map<Integer, Integer> ColstopologyScoreMap = Map.of(
        -1, 500,
        0, 100,
        1, 10,
        2, 1,
        3, 0
    );

    public static double calculateP2PSpread(InstanceTopology instanceTopology, ClosTopology closTopology) {
        double score = 0;
        List<Instance> instances = instanceTopology.getAllInstances();
        for (Instance instance : instances) {
            int hostId = instance.getHost();
            List<Instance> linkedInstances = instanceTopology.getLinkedInstances(instance);
            for (Instance linkedInstance : linkedInstances) {
                if (hostId == linkedInstance.getHost()) {
                    score += ColstopologyScoreMap.get(-1);
                } else {
                    ClosTopology sameClosTopology = closTopology.getNearestCommonFatherTopology(List.of(hostId, linkedInstance.getHost()));
                    score += ColstopologyScoreMap.get(sameClosTopology.getLevel());
                }
            }
        }
        return score/2;
    }

    public static double calculateAll2AllSpread(InstanceTopology instanceTopology, ClosTopology closTopology) {
        double score = 0;
        List<Instance> instances = instanceTopology.getAllInstances();
        if(instances.isEmpty()) {
            return score;
        }
        List<Integer> hostIds = instances.stream().map(Instance::getHost).distinct().toList();
        // hostids中的元素是否全部相同
        int hostId0 = hostIds.get(0);
        boolean allSame = hostIds.stream().allMatch(id -> id == hostId0);
        if (allSame) {
            score = 500 * instances.size();
        } else {
            ClosTopology sameClosTopology = closTopology.getNearestCommonFatherTopology(hostIds);
            score = ColstopologyScoreMap.get(sameClosTopology.getLevel()) * instances.size();
        }

        return score;
    }

    // 计算分散度得分
    // subTopology name, ig id, ur id, score（对于P2P来说，记录各个link的情况,同host 500，同S0 100， 同S1 10， 同S2 1， 同S3 无）
    public static double calculateScheduledTopologyScore(InstanceTopology instanceTopology, ClosTopology closTopology) {
        double score = 0;
        switch (instanceTopology.getType()) {
            case InstanceTopology.P2P:
                // 计算P2P的分散度得分
                score = calculateP2PSpread(instanceTopology, closTopology);
                break;
            case InstanceTopology.All2All:
                // 计算ALL2ALL的分散度得分
                score = calculateAll2AllSpread(instanceTopology, closTopology);
                break;
            default:
                break;
        } 
        return score;
    }
}
