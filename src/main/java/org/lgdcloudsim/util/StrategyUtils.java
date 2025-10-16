package org.lgdcloudsim.util;

import java.util.HashSet;
import java.util.List;

import org.lgdcloudsim.network.ClosTopologyItem;
import org.lgdcloudsim.network.ClosTopologyManager;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.TrainingStrategy;

public class StrategyUtils {
    static int calculateDPSpread(InstanceGroup instanceGroup, ClosTopologyManager closTopologyManager) {
        int spread = 0;
        TrainingStrategy trainingStrategy = instanceGroup.getTrainingStrategy();
        if (trainingStrategy == null) {
            return spread;
        }
        
        int dpDim = trainingStrategy.getDPDim();
        int ppDim = trainingStrategy.getPPDim();
        for(int i = 0; i < dpDim; i++) {
            HashSet<String> pswSet = new HashSet<>();
            List<Instance> instanceList = instanceGroup.getInstances();
            for(int j = 0; j < ppDim; j++) {
                Instance instance = instanceList.get(i * ppDim + j);
                int hostId = instance.getHost();
                ClosTopologyItem closItem = closTopologyManager.getClosTopologyItemByHostId(hostId);
                pswSet.add(closItem.getPSW());
            }
            if (pswSet.size() > spread) {
                spread = pswSet.size();
            }
        }
        
        return spread;
    }

    static int calculatePPSpread(InstanceGroup instanceGroup, ClosTopologyManager closTopologyManager) {
        int spread = 0;
        TrainingStrategy trainingStrategy = instanceGroup.getTrainingStrategy();
        if (trainingStrategy == null) {
            return spread;
        }
        
        int dpDim = trainingStrategy.getDPDim();
        int ppDim = trainingStrategy.getPPDim();
        for(int i = 0; i < ppDim; i++) {
            HashSet<String> pswSet = new HashSet<>();
            List<Instance> instanceList = instanceGroup.getInstances();
            for(int j = 0; j < dpDim; j++) {
                Instance instance = instanceList.get(j * ppDim + i);
                int hostId = instance.getHost();
                ClosTopologyItem closItem = closTopologyManager.getClosTopologyItemByHostId(hostId);
                pswSet.add(closItem.getPSW());
            }
            if (pswSet.size() > spread) {
                spread = pswSet.size();
            }
        }
        return spread;    
    }

    static double calculateSpreadScore(InstanceGroup instanceGroup, ClosTopologyManager closTopologyManager, double alpha, double beta) {
        int dpSpread = calculateDPSpread(instanceGroup, closTopologyManager);
        int ppSpread = calculatePPSpread(instanceGroup, closTopologyManager);
        return alpha * dpSpread + beta * ppSpread;  
    }
}
