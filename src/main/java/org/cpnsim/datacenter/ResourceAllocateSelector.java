package org.cpnsim.datacenter;

import org.cloudsimplus.core.DatacenterEntity;
import org.cpnsim.innerscheduler.InnerScheduleResult;
import org.cpnsim.request.Instance;
import org.cpnsim.request.InstanceGroup;

import java.util.List;
import java.util.Map;

/**
 * An interface to be implemented by each class that represents a resource allocator.
 * When multiple {@link Instance}s are assigned to the same host at the same time,
 * it is necessary to select which {@link Instance}s are placed on the host
 * and which are not placed on the host through it.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface ResourceAllocateSelector extends DatacenterEntity {
    /**
     * Select the {@link Instance}s to be placed on the host.
     *
     * @param innerScheduleResults the inner schedule results
     * @return the result of the resource allocation
     */
    ResourceAllocateResult selectResourceAllocate(List<InnerScheduleResult> innerScheduleResults);

    /**
     * Get the number of conflicts when resource allocating.
     */
    Map<Integer, Integer> getPartitionConflicts();

    List<InstanceGroup> filterConflictedInstanceGroup(List<InstanceGroup> instanceGroups);
}
