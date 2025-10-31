package org.lgdcloudsim.network;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClosTopologyCondition {
    long topologyScoreSum;
    double s0Pct30TopologyScore;
    double s0Pct60TopologyScore;
    double s0Pct90TopologyScore;
    double s0MeanTopologyScore;

    public ClosTopologyCondition(long topologyScoreSum, double s0Pct30TopologyScore, double s0Pct60TopologyScore, double s0Pct90TopologyScore, double s0MeanTopologyScore) {
        this.topologyScoreSum = topologyScoreSum;
        this.s0Pct30TopologyScore = s0Pct30TopologyScore;
        this.s0Pct60TopologyScore = s0Pct60TopologyScore;
        this.s0Pct90TopologyScore = s0Pct90TopologyScore;
        this.s0MeanTopologyScore = s0MeanTopologyScore;
    }
}
