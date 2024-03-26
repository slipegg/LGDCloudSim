package org.cpnsim.interscheduler;

import org.cpnsim.core.Simulation;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.request.InstanceGroup;

import java.util.List;
import java.util.Map;

public class InterSchedulerHFRS extends InterSchedulerSimple{
    public InterSchedulerHFRS(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        super(id, simulation, collaborationId, target, isSupportForward);
    }
    @Override
    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups){
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(collaborationId, target, isSupportForward, allDatacenters);

        for(InstanceGroup instanceGroup : instanceGroups){
            Map<InstanceGroup, List<Datacenter>> instanceGroupAvailableDatacenters = RandomAndHeuristicAlgorithm.heuristicFiltering(instanceGroup, allDatacenters, simulation, interScheduleSimpleStateMap);

            RandomAndHeuristicAlgorithm.randomScoring(interSchedulerResult, instanceGroupAvailableDatacenters, interScheduleSimpleStateMap);    
        }

        return interSchedulerResult;
    }
}
