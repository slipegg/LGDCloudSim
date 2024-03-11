package org.cpnsim.interscheduler;

import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;

import java.util.List;
import java.util.Map;

public class InterSchedulerRFHS extends InterSchedulerSimple{
    public InterSchedulerRFHS(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        super(id, simulation, collaborationId, target, isSupportForward);
    }
    @Override
    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups){
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, isSupportForward, allDatacenters);

        Double RandomRate = 0.3;
        Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = RandomAndHeuristicAlgorithm.randomFiltering(instanceGroups, allDatacenters, RandomRate);

        RandomAndHeuristicAlgorithm.heuristicScoring(interSchedulerResult, instanceGroupAvailableDatacenters, interScheduleSimpleStateMap);

        return interSchedulerResult;
    }
}
