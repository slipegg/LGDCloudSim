package org.lgdcloudsim.shadowresource.filter;

import java.util.ArrayList;
import java.util.List;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SRRequestFilterRes {
    private List<Instance> normalInstances;
    private List<SRRequest> SRInstacnes;

    public SRRequestFilterRes(List<Instance> normalInstances, List<SRRequest> SRInstacnes) {
        this.normalInstances = normalInstances;
        this.SRInstacnes = SRInstacnes;
    }

    public SRRequestFilterRes() {
        normalInstances = new ArrayList<>();
        SRInstacnes = new ArrayList<>();
    }

    public SRRequestFilterRes add(Instance instance) {
        normalInstances.add(instance);
        return this;
    }

    public SRRequestFilterRes add(SRRequest srRequest) {
        SRInstacnes.add(srRequest);
        return this;
    }
}
