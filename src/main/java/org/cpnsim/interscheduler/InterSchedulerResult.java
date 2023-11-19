package org.cpnsim.interscheduler;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class InterSchedulerResult {
    private int collaborationId;//给集中调度器用的
    private int target;
    private Boolean isSupportForward;
    private Map<Datacenter, List<InstanceGroup>> scheduledResultMap;
    private List<InstanceGroup> failedInstanceGroups;
    private int instanceGroupNum;

    public InterSchedulerResult(int collaborationId, int target, Boolean isSupportForward, List<Datacenter> allDatacenters) {
        this.collaborationId = collaborationId;
        this.target = target;
        this.isSupportForward = isSupportForward;
        this.failedInstanceGroups = new ArrayList<>();
        initDcResultMap(allDatacenters);

    }

    public InterSchedulerResult(int collaborationId, int target, List<Datacenter> allDatacenters) {
        this(collaborationId, target, false, allDatacenters);
    }

    public void addDcResult(InstanceGroup instanceGroup, Datacenter datacenter) {
        this.scheduledResultMap.get(datacenter).add(instanceGroup);
        this.instanceGroupNum++;
    }

    public void addFailedInstanceGroup(InstanceGroup instanceGroup) {
        this.failedInstanceGroups.add(instanceGroup);
        this.instanceGroupNum++;
    }

    private void initDcResultMap(List<Datacenter> datacenters) {
        this.scheduledResultMap = new HashMap<>();
        for (Datacenter datacenter : datacenters) {
            this.scheduledResultMap.put(datacenter, new ArrayList<>());
        }
    }

    public boolean isScheduledInstanceGroupsEmpty() {
        int sum = scheduledResultMap.values().stream().mapToInt(List::size).sum();
        return sum == 0;
    }
}