package org.lgdcloudsim.shadowresource.filter;

import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.shadowresource.partitionmanager.SRCoordinator;

import java.util.List;

public interface SRRequestFilter {
    SRRequestFilterRes filter(List<Instance> instances);

    SRRequestFilter setSrCoordinator(SRCoordinator srCoordinator);
}
