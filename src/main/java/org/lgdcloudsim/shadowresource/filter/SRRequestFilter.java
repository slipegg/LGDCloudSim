package org.lgdcloudsim.shadowresource.filter;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.requestmapper.SRRequest;

import java.util.List;

public interface SRRequestFilter {
    SRRequestFilterRes filter(Instance instance);

    SRRequestFilterRes filter(List<Instance> instances);
}
