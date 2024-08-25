package org.lgdcloudsim.shadowresource.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SRRequestFilterRes {
    private List<Instance> normalInstances;
    private Map<Integer, List<SRRequest>> distributedSRMap;

    public SRRequestFilterRes(List<Instance> normalInstances, Map<Integer, List<SRRequest>> distributedSRMap) {
        this.normalInstances = normalInstances;
        this.distributedSRMap = distributedSRMap;
    }

    public SRRequestFilterRes() {
        normalInstances = new ArrayList<>();
        distributedSRMap = new HashMap<>();
    }

    public SRRequestFilterRes add(Instance instance) {
        normalInstances.add(instance);
        return this;
    }

    public SRRequestFilterRes add(int partitionId, List<SRRequest> srRequests) {
        distributedSRMap.putIfAbsent(partitionId, new ArrayList<>());
        distributedSRMap.get(partitionId).addAll(srRequests);
        return this;
    }
}
