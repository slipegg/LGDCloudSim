package org.cpnsim.interscheduler;

import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;

import java.util.List;

public class InterSchedulerRound extends InterSchedulerSimple{
    private int lastSendDCIndex = 0;
    public InterSchedulerRound(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        super(id, simulation, collaborationId, target, isSupportForward);
    }
    @Override
    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, isSupportForward, allDatacenters);

        for (InstanceGroup instanceGroup : instanceGroups) {
            Datacenter target = allDatacenters.getValue().get(random.nextInt(allDatacenters.size()));
            interSchedulerResult.addDcResult(instanceGroup, target);
        }

        return interSchedulerResult;
    }
}
