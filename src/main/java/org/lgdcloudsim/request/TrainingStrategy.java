package org.lgdcloudsim.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;

@Getter
public class TrainingStrategy {
    public static final String TYPE_PS = "PS";
    public static final String TYPE_DP = "DP";
    public static final String TYPE_3D_DP = "3D_DP";
    public static final String TYPE_3D_PP = "3D_PP";

    String strategy_type;
    int DPDim;
    int PPDim;
    int TPDim;

    int rank0Index;

    // Score: Topology
    Map<Integer, List<InstanceTopology>> instanceTopologyMap;

    /**
     * Construct a TrainingStrategy with the given DPDim and PPDim.
     * @param DPDim
     * @param PPDim
     */
    public TrainingStrategy(String strategy_type, int DPDim, int PPDim, int rank0Index, List<Instance> instanceList) {
        this.strategy_type = strategy_type;
        this.DPDim = DPDim;
        this.PPDim = PPDim;
        this.TPDim = 1;
        this.rank0Index = rank0Index;
        // 倒序排列
        this.instanceTopologyMap = new TreeMap<>(java.util.Collections.reverseOrder());

        switch (strategy_type) {
            case TYPE_PS: {
                // PS-specific topology
                constructTopologyForPS(instanceList);
                break;
            }
            case TYPE_DP: {
                constructTopologyForDP(instanceList);
                break;
            }
            case TYPE_3D_DP: {
                // 3D-specific topology
                constructTopologyFor3D(instanceList, DPDim, PPDim, true);
                break;
            }
            case TYPE_3D_PP: {
                // 3D-specific topology
                constructTopologyFor3D(instanceList, DPDim, PPDim, false);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown training strategy type: " + strategy_type);
        }
    }

    private void constructTopologyForPS(List<Instance> instanceList) {
        InstanceTopologyP2P topology = new InstanceTopologyP2P();
        Instance masterInstance = instanceList.get(0);
        List<Instance> workerInstances = instanceList.subList(1, instanceList.size());
        topology.addLink(masterInstance, workerInstances);
        this.instanceTopologyMap.put(100, List.of(topology));
    }

    private void constructTopologyForDP(List<Instance> instanceList) {
        // DP-specific topology
        InstanceTopologyAll2All topology = new InstanceTopologyAll2All(instanceList);
        this.instanceTopologyMap.put(100, List.of(topology));
    }

    private void constructTopologyFor3D(List<Instance> instanceList, int dpDim, int ppDim, boolean isDPFirst) {
        // 3D-specific topology
        if (isDPFirst){
            List<InstanceTopology> dpTopologyList = new ArrayList<>();
            for (int pp = 0; pp < ppDim; pp++) {
                List<Instance> dpGroupInstances = new ArrayList<>();
                for (int dp = 0; dp < dpDim; dp++) {
                    int index = pp * dpDim + dp;
                    dpGroupInstances.add(instanceList.get(index));
                }
                InstanceTopologyAll2All topology = new InstanceTopologyAll2All(dpGroupInstances);
                dpTopologyList.add(topology);
            }
            this.instanceTopologyMap.put(100, dpTopologyList);
        } else {
            List<InstanceTopology> ppTopologyList = new ArrayList<>();
            for (int dp = 0; dp < dpDim; dp++) {
                InstanceTopologyP2P topology = new InstanceTopologyP2P();
                Instance lastInstance = null;
                for (int pp = 0; pp < ppDim; pp++) {
                    int index = pp * dpDim + dp;
                    if (lastInstance != null) {
                        List<Instance> linkedInstances = new ArrayList<>();
                        linkedInstances.add(instanceList.get(index));
                        topology.addLink(lastInstance, linkedInstances);
                    }
                    lastInstance = instanceList.get(index);
                }
                ppTopologyList.add(topology);
            }
            this.instanceTopologyMap.put(100, ppTopologyList);
        }
    }

    public boolean isOnlyOneSubInstanceTopology() {
        int subTopologiesNum = 0;
        for (List<InstanceTopology> topologyList : instanceTopologyMap.values()) {
            subTopologiesNum += topologyList.size();
            if (subTopologiesNum > 1) {
                return false;
            }
        }
        return true;
    }

    public int getDPRank(int instanceId) {
        return (instanceId - rank0Index) / DPDim;
    }

    public int getPPRank(int instanceId) {
        return (instanceId - rank0Index) % PPDim;
    }

    public int getSubInstanceTopologyCount() {
        int count = 0;
        for (List<InstanceTopology> topologyList : instanceTopologyMap.values()) {
            count += topologyList.size();
        }
        return count;
    }
}
