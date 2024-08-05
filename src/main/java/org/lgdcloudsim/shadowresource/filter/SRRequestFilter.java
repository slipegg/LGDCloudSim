package org.lgdcloudsim.shadowresource.filter;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import java.util.List;

public interface SRRequestFilter {
    SRRequest filter(Instance instance);

    List<SRRequest> filter(List<Instance> instances);
}
