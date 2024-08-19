package org.lgdcloudsim.shadowresource.util;

import java.util.List;

import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SRRequestScheduledRes {
    public int partitionId;
    public List<SRRequest> scheduledSRRequest;
    public double cost;

    public SRRequestScheduledRes(int partitionId, List<SRRequest> scheduledRequests, double scheduledCost) {
        this.partitionId = partitionId;
        this.scheduledSRRequest = scheduledRequests;
        this.cost = scheduledCost;
    }
}
