package org.lgdcloudsim.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.network.ClosTopology;
import org.lgdcloudsim.network.ClosTopologyItem;
import org.lgdcloudsim.network.ClosTopologyManager;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.InstanceTopology;
import org.lgdcloudsim.request.InstanceTopologyCondition;
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
        return getMaxSameLevelFromInstances(instanceTopology.getAllInstances(), closTopology);
    }

    public static TopologyLevelResult getMaxSameLevel(List<InstanceTopology> instanceTopologies, ClosTopology closTopology) {
        List<Instance> instances = new ArrayList<>();
        for (InstanceTopology instanceTopology : instanceTopologies) {
            instances.addAll(instanceTopology.getAllInstances());
        }
        return getMaxSameLevelFromInstances(instances, closTopology);
    }

    private static TopologyLevelResult getMaxSameLevelFromInstances(List<Instance> instances, ClosTopology closTopology) {
        int maxLevel = -2;
        String name = "";
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

    public static InstanceTopologyCondition getInstanceTopologyCondition(InstanceTopology instanceTopology, ClosTopology closTopology) {
        InstanceTopologyCondition instanceTopologyCondition = new InstanceTopologyCondition();
        switch (instanceTopology.getType()) {
            case InstanceTopology.P2P:
                instanceTopologyCondition = getP2PInstanceTopologyCondition(instanceTopology, closTopology);
            case InstanceTopology.All2All:
                instanceTopologyCondition = getAll2AllInstanceTopologyCondition(instanceTopology, closTopology);
            default:
                break;
        }
        return instanceTopologyCondition;
    }

    private static InstanceTopologyCondition getP2PInstanceTopologyCondition(InstanceTopology instanceTopology, ClosTopology closTopology) {
        InstanceTopologyCondition condition = new InstanceTopologyCondition();
        List<Instance> instances = instanceTopology.getAllInstances();
        for (Instance instance : instances) {
            int hostId = instance.getHost();
            List<Instance> linkedInstances = instanceTopology.getLinkedInstances(instance);
            for (Instance linkedInstance : linkedInstances) {
                int linkedHostId = linkedInstance.getHost();
                if (hostId == linkedHostId) {
                    condition.setSameHostNum(condition.getSameHostNum() + 1);
                } else {
                    ClosTopology sameClosTopology = closTopology.getNearestCommonFatherTopology(List.of(hostId, linkedHostId));
                    switch (sameClosTopology.getLevel()) {
                        case 0:
                            condition.setSameS0Num(condition.getSameS0Num() + 1);
                            break;
                        case 1:
                            condition.setSameS1Num(condition.getSameS1Num() + 1);
                            break;
                        case 2:
                            condition.setSameS2Num(condition.getSameS2Num() + 1);
                            break;
                        case 3:
                            condition.setSameS3Num(condition.getSameS3Num() + 1);
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        condition.setSameHostNum(condition.getSameHostNum() / 2);
        condition.setSameS0Num(condition.getSameS0Num() / 2);
        condition.setSameS1Num(condition.getSameS1Num() / 2);
        condition.setSameS2Num(condition.getSameS2Num() / 2);
        condition.setSameS3Num(condition.getSameS3Num() / 2);
        
        return condition;
    }

    private static InstanceTopologyCondition getAll2AllInstanceTopologyCondition(InstanceTopology instanceTopology, ClosTopology closTopology) {
        InstanceTopologyCondition condition = new InstanceTopologyCondition();
        List<Instance> instances = instanceTopology.getAllInstances();
        List<Integer> hostIds = instances.stream().map(Instance::getHost).distinct().toList();
        // hostids中的元素是否全部相同
        int hostId0 = hostIds.get(0);
        boolean allSame = hostIds.stream().allMatch(id -> id == hostId0);
        int linkNum = instances.size() - 1;
        if (allSame) {
            condition.setSameHostNum(linkNum);
        } else {
            ClosTopology sameClosTopology = closTopology.getNearestCommonFatherTopology(hostIds);
            int level = sameClosTopology.getLevel();
            switch (level) {
                case 0:
                    condition.setSameS0Num(linkNum);
                    break;
                case 1:
                    condition.setSameS1Num(linkNum);
                    break;
                case 2:
                    condition.setSameS2Num(linkNum);
                    break;
                case 3:
                    condition.setSameS3Num(linkNum);
                    break;
                default:
                    break;
            }
        }
        return condition;
    }
}
