package org.lgdcloudsim.interscheduler;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

import java.util.*;

/**
 * The scheduling result of the inter-scheduler.
 * It contains the scheduling result of the instance groups and the failed instance groups.
 * It also contains the outdated user requests which are exceeded the schedule delay limit.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
@Getter
@Setter
public class InterSchedulerResult {
    /**
     * The interScheduler of the result.
     */
    private InterScheduler interScheduler;

    /**
     * The scheduling result map of the instance groups.
     * For the host-target schedule result, the schedule result has been stored in every instance's {@link Instance#getExpectedScheduleHostId()}.
     * TODO needs to be changed from List to Set later to speed up the determination of inclusion.
     */
    private Map<Datacenter, List<InstanceGroup>> scheduledResultMap;

    /**
     * The failed instance groups.
     */
    private List<InstanceGroup> failedInstanceGroups;

    /**
     * The number of the instance groups.
     */
    private int instanceGroupNum;

    /**
     * The outdated user requests which are exceeded the schedule delay limit.
     */
    private Set<UserRequest> outDatedUserRequests;

    /**
     * Create a new inter-scheduler result.
     * @param interScheduler the collaboration id of the inter-scheduler.
     * @param allDatacenters all the datacenters in the collaboration zone.
     */
    public InterSchedulerResult(InterScheduler interScheduler, List<Datacenter> allDatacenters) {
        this.interScheduler = interScheduler;
        this.failedInstanceGroups = new ArrayList<>();
        initDcResultMap(allDatacenters);
    }

    /**
     * Add the scheduled instance group to the scheduling result map.
     * @param instanceGroup the scheduled instance group.
     * @param datacenter the datacenter where the instance group is scheduled.
     */
    public void addDcResult(InstanceGroup instanceGroup, Datacenter datacenter) {
        this.scheduledResultMap.get(datacenter).add(instanceGroup);
        this.instanceGroupNum++;
    }

    /**
     * Add the failed instance group to the failed instance groups.
     * @param instanceGroup the failed instance group.
     */
    public void addFailedInstanceGroup(InstanceGroup instanceGroup) {
        this.failedInstanceGroups.add(instanceGroup);
        this.instanceGroupNum++;
    }

    /**
     * Initialize the scheduling result map.
     * @param datacenters the datacenters.
     */
    private void initDcResultMap(List<Datacenter> datacenters) {
        this.scheduledResultMap = new HashMap<>();
        for (Datacenter datacenter : datacenters) {
            this.scheduledResultMap.put(datacenter, new ArrayList<>());
        }
    }

    /**
     * Get the scheduled datacenter of the instance group.
     * @param instanceGroup the instance group.
     * @return the scheduled datacenter of the instance group.
     */
    public Datacenter getScheduledDatacenter(InstanceGroup instanceGroup) {
        for (Map.Entry<Datacenter, List<InstanceGroup>> scheduledResult : scheduledResultMap.entrySet()) {
            if (scheduledResult.getValue().contains(instanceGroup)) {
                return scheduledResult.getKey();
            }
        }
        return Datacenter.NULL;
    }

    /**
     * Get the target of the inter-scheduler.
     *
     * @return the target of the inter-scheduler.
     */
    public int getTarget() {
        return interScheduler.getTarget();
    }

    /**
     * Get whether the scheduled instance group results support forward again.
     *
     * @return whether the scheduled instance group results support forward again.
     */
    public Boolean isSupportForward() {
        return interScheduler.isSupportForward();
    }
}
