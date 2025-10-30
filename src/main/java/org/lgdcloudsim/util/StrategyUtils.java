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

    /**
     * 用于封装getMaxSameLevel方法的返回结果
     */
    public static class TopologyLevelResult {
        private final int maxLevel;
        private final String name;

        public TopologyLevelResult(int maxLevel, String name) {
            this.maxLevel = maxLevel;
            this.name = name;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "TopologyLevelResult{maxLevel=" + maxLevel + ", name='" + name + "'}";
        }
    }

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

    /**
     * 获取实例拓扑中主机所在的最大相同层级
     * @param instanceTopology 实例拓扑
     * @param closTopology Clos拓扑
     * @return 包含maxLevel和name的结果对象
     */
    public static TopologyLevelResult getMaxSameLevel(InstanceTopology instanceTopology, ClosTopology closTopology) {
        int maxLevel = -2;
        String name = "";
        List<Instance> instances = instanceTopology.getAllInstances();
        List<Integer> hostIds = instances.stream().map(Instance::getHost).distinct().toList();
        // hostids中的元素是否全部相同
        int hostId0 = hostIds.get(0);
        boolean allSame = hostIds.stream().allMatch(id -> id == hostId0);
        if (allSame) {
            maxLevel = -1;
            name = String.valueOf(hostId0); // 将hostId0转换为字符串格式的name
        } else {
            ClosTopology sameClosTopology = closTopology.getNearestCommonFatherTopology(hostIds);
            maxLevel = sameClosTopology.getLevel();
            name = sameClosTopology.getName();
        }
        return new TopologyLevelResult(maxLevel, name);
    }
}
