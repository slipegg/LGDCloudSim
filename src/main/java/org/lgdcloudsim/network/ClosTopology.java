package org.lgdcloudsim.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;

import org.lgdcloudsim.statemanager.StatesManager;
import org.lgdcloudsim.util.Range;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClosTopology {
    String name;
    long topologyScore;
    int level;
    Range range;
    List<ClosTopology> subTopologys;
    Set<String> existingSubTopologyNames;
    ClosTopology fatherTopology;
    Map<Integer, Integer> candidateReplicateMap; // hostID -> suited replicate num
    int specialHostID = -1;

    public ClosTopology(String name, int level) {
        this.name = name;
        this.subTopologys = new ArrayList<>();
        this.existingSubTopologyNames = new HashSet<>();
        this.candidateReplicateMap = new HashMap<>();
        this.topologyScore = 0;
        this.level = level;
    }

    public ClosTopology(String name, int level, int minID, int maxID) {
        this(name, level);
        this.range = new Range(minID, maxID);
    }

    public ClosTopology AddClosTopology(String S2Name, String S1Name, String S0Name, int minID, int maxID) {
        if (!this.existingSubTopologyNames.contains(S2Name)) {
            ClosTopology S2Topology = new ClosTopology(S2Name, 2);
            setSubTopology(S2Topology);
            this.existingSubTopologyNames.add(S2Name);
            S2Topology.AddClosTopology(S1Name, S0Name, minID, maxID);
        } else {
            for (ClosTopology sub : this.subTopologys) {
                if (sub.name.equals(S2Name)) {
                    sub.AddClosTopology(S1Name, S0Name, minID, maxID);
                    break;
                }
            }
        }
        return this;
    }
    
    public ClosTopology AddClosTopology(String S1Name, String S0Name, int minID, int maxID) {
        if (!this.existingSubTopologyNames.contains(S1Name)) {
            ClosTopology S1Topology = new ClosTopology(S1Name, 1);
            setSubTopology(S1Topology);
            this.existingSubTopologyNames.add(S1Name);
            S1Topology.AddClosTopology(S0Name, minID, maxID);
        } else {
            for (ClosTopology sub : this.subTopologys) {
                if (sub.name.equals(S1Name)) {
                    sub.AddClosTopology(S0Name, minID, maxID);
                    break;
                }
            }
        }
        return this;
    }
    
    public ClosTopology AddClosTopology(String S0Name, int minID, int maxID) {
        if (!this.existingSubTopologyNames.contains(S0Name)) {
            ClosTopology S0Topology = new ClosTopology(S0Name, 0, minID, maxID);
            setSubTopology(S0Topology);
            this.existingSubTopologyNames.add(S0Name);
        }
        return this;
    }

    public ClosTopology setSubTopology(ClosTopology subTopology) {
        for (ClosTopology sub : this.subTopologys) {
            if (sub.name.equals(subTopology.name)) {
                return this;
            }
        }
        this.subTopologys.add(subTopology);
        subTopology.setFatherTopology(this);
        if (subTopology.isLeafTopology()) {
            updateFatherTopologyRange(subTopology.getRange());
        }
        return this;
    }

    private void updateFatherTopologyRange(Range subRange) {
        if (this.range == null) {
            this.range = new Range(subRange.getMin(), subRange.getMax());
        } else {
            this.range.merge(subRange);
        }
        if (this.fatherTopology != null) {
            this.fatherTopology.updateFatherTopologyRange(this.range);
        }
    }

    public boolean isLeafTopology() {
        return this.level == 0;
    }

    public boolean isExistSubTopology(String subTopologyName) {
        for (ClosTopology sub : this.subTopologys) {
            if (sub.name.equals(subTopologyName)) {
                return true;
            }
        }
        return false;
    }

    public List<ClosTopology> getSortedSubTopologies(boolean ascending) {
        if (ascending) {
            this.subTopologys.sort(Comparator.comparingLong(ClosTopology::getTopologyScore));
        } else {
            this.subTopologys.sort(Comparator.comparingLong(ClosTopology::getTopologyScore).reversed());
        }
        return this.subTopologys;
    }

    public ClosTopology InitTopologyGPU(StatesManager statesManager) {
        if (this.level == 0) {
            long topologyScoreSum = 0;
            int rangeNum = this.range.getMax() - this.range.getMin() + 1;
            for (int hostID = (int) this.range.getMin(); hostID <= (int) this.range.getMax(); hostID++) {
                topologyScoreSum += statesManager.getActualHostState(hostID).getGPUScore() * rangeNum;
            }
            this.topologyScore = topologyScoreSum;
        } else {
            long topologyScoreSum = 0;
            for (ClosTopology subTopology : this.subTopologys) {
                subTopology.InitTopologyGPU(statesManager);
                topologyScoreSum += subTopology.getTopologyScore() * this.subTopologys.size();
            }
            this.topologyScore = topologyScoreSum;
        }
        return this;
    }

    public ClosTopology updateHostTopologyScore(int originalGPU, int nowGPU) {
        if (!this.isLeafTopology()) {
            throw new IllegalArgumentException("Only leaf topology can update host topology score.");
        }
        long changedScore = (nowGPU * (nowGPU-1) - originalGPU * (originalGPU - 1)) * (this.range.getMax() - this.range.getMin() + 1);
        this.topologyScore += changedScore;
        ClosTopology father = this.fatherTopology;
        while (father != null) {
            father.topologyScore += changedScore * father.subTopologys.size();
            father = father.fatherTopology;
        }
        return this;
    }

    public ClosTopology AddHost(int hostID, int suitedReplicate) {
        this.candidateReplicateMap.put(hostID, suitedReplicate);
        return this;
    }

    public ClosTopology clearCandidate() {
        this.candidateReplicateMap.clear();
        for (ClosTopology subTopology : this.subTopologys) {
            subTopology.clearCandidate();
        }
        return this;
    }

    public int getCandidateReplicateSum() {
        int sum = 0;
        if (isLeafTopology()) {
            for (Integer replicateNum : this.candidateReplicateMap.values()) {
                sum += replicateNum;
            }
        } else {
            for (ClosTopology subTopology : this.subTopologys) {
                sum += subTopology.getCandidateReplicateSum();
            }
        }
        return sum;
    }

    public int getMostCandidateReplicateHostID() {
        int maxReplicate = -1;
        int maxHostID = -1;
        if (this.candidateReplicateMap.size() == 0 || !isLeafTopology()) {
            return maxHostID;
        }

        for (Map.Entry<Integer, Integer> entry : this.candidateReplicateMap.entrySet()) {
            if (entry.getValue() > maxReplicate) {
                maxReplicate = entry.getValue();
                maxHostID = entry.getKey();
            }
        }
        return maxHostID;
    }

    public ClosTopology updateByAllocate(int hostID) {
        if (isLeafTopology()) {
            if (this.candidateReplicateMap.containsKey(hostID)) {
                this.candidateReplicateMap.put(hostID, this.candidateReplicateMap.get(hostID) - 1);
            }
        }
        return this;
    }

    private int findCandidateReplicate() {
        for (Map.Entry<Integer, Integer> entry : this.candidateReplicateMap.entrySet()) {
            int hostID = entry.getKey();
            int suitedReplicate = entry.getValue();
            if (suitedReplicate > 0) {
                return hostID;
            }
        }
        return -1;
    }

    public ClosTopology getClosestHosts(int targetHostID, int rootLevel) {
        if (!isLeafTopology()) {
            return null;
        }

        // 尝试放入同一个主机上
        int remainingReplicateInSameHost = candidateReplicateMap.getOrDefault(targetHostID, 0);
        if (remainingReplicateInSameHost > 0) {
            this.specialHostID = targetHostID;
            return this;
        }

        // 尝试放入同一个S0交换机下的其他主机上
        int hostID = findCandidateReplicate();
        if (hostID != -1) {
            this.specialHostID = hostID;
            return this;
        }

        // 尝试放入同一个S1交换机下的其他主机上
        ClosTopology S1Topology = this.fatherTopology;
        if (S1Topology != null && rootLevel >= 1) {
            for (ClosTopology S0Topology : S1Topology.getSortedSubTopologies(true)) {
                if (S0Topology.name.equals(this.name)) {
                    continue;
                }
                hostID = S0Topology.findCandidateReplicate();
                if (hostID != -1) {
                    S0Topology.specialHostID = hostID;
                    return S0Topology;
                }
            }
        }

        // 尝试放入同一个S2交换机下的其他主机上
        ClosTopology S2Topology = S1Topology.fatherTopology;
        if (S2Topology != null && rootLevel >= 2) {
            for (ClosTopology otherS1Topology : S2Topology.getSortedSubTopologies(true)) {
                if (otherS1Topology.name.equals(S1Topology.name)) {
                    continue;
                }
                for (ClosTopology S0TopologyTmp : otherS1Topology.getSortedSubTopologies(true)) {
                    hostID = S0TopologyTmp.findCandidateReplicate();
                    if (hostID != -1) {
                        S0TopologyTmp.specialHostID = hostID;
                        return S0TopologyTmp;
                    }
                }
            }
        }

        // 尝试放入同一个DC下的其他主机上
        ClosTopology S3Topology = S2Topology.fatherTopology;
        if (S3Topology != null && rootLevel >= 3) {
            ClosTopology dcTopology = S3Topology.fatherTopology;
            for (ClosTopology S2TopologyTmp : dcTopology.subTopologys) {
                if (S2TopologyTmp.name.equals(S2Topology.name)) {
                    continue;
                }
                for (ClosTopology otherS1Topology : S2TopologyTmp.subTopologys) {
                    for (ClosTopology S0TopologyTmp : otherS1Topology.subTopologys) {
                        hostID = S0TopologyTmp.findCandidateReplicate();
                        if (hostID != -1) { 
                            S0TopologyTmp.specialHostID = hostID;
                            return S0TopologyTmp;
                        }
                    }
                }
            }
        }
        if (hostID == -1) {
            throw new IllegalArgumentException("No candidate host found for replicate. But the pre filter succeeded.");
        }
        return null;
    }

    public ClosTopology getNearestCommonFatherTopology(List<Integer> hostIDs) {
        if (this.range.containsAll(hostIDs)) {
            if (this.isLeafTopology()) {
                return this;
            } else {
                for (ClosTopology subTopology : this.subTopologys) {
                    if (subTopology.range.containsAll(hostIDs)) {
                        return subTopology.getNearestCommonFatherTopology(hostIDs);
                    }
                }
                return this;
            }
        }

        ClosTopology father = this.fatherTopology;
        while (father != null) {
            if (father.range.containsAll(hostIDs)) {
                return father.getNearestCommonFatherTopology(hostIDs);
            }
            father = father.fatherTopology;
        }

        return null;
    }

    public List<ClosTopology> getS0ClosTopologies() {
        List<ClosTopology> S0Topologies = new ArrayList<>();
        if (this.isLeafTopology()) {
            S0Topologies.add(this);
        } else {
            for (ClosTopology subTopology : this.subTopologys) {
                S0Topologies.addAll(subTopology.getS0ClosTopologies());
            }
        }
        return S0Topologies;
    }

    @Override
    public String toString() {
        return "ClosTopology{" +
                "name='" + name + '\'' +
                ", topologyScore=" + topologyScore +
                ", level=" + level +
                ", range=" + range +
                '}';
    }
}
