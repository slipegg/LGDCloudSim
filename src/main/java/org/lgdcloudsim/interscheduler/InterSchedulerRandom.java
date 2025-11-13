package org.lgdcloudsim.interscheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.interscheduler.InterSchedulerLeastRequested.CustomComparator;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.statemanager.simplestate.ClosTopologyStateSimple;
import org.lgdcloudsim.statemanager.simplestate.DcClosTopologyStateSimple;
import org.lgdcloudsim.statemanager.simplestate.SimpleStateEasy;
import org.lgdcloudsim.statemanager.simplestate.SimpleStateEasyObject;

public class InterSchedulerRandom extends InterSchedulerSimple {
        /**
     * The constructor of the clos topology inter-scheduler.
     *
     * @param id               the id of the inter-scheduler.
     * @param simulation       the simulation object.
     * @param collaborationId  the collaboration id of the inter-scheduler.
     * @param target           the target id of the inter-scheduler.
     * @param isSupportForward whether the inter-scheduler supports forward.
     */
    public InterSchedulerRandom(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        super(id, simulation, collaborationId, target, isSupportForward);
    }

    /**
     * Schedule the instance groups to the data centers.
     * @param instanceGroups the instance groups to be scheduled
     * @return the result of the scheduling.
     */
    @Override
    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups) {
        final List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(this, allDatacenters);

        Map<Datacenter, Object> interScheduleSimpleStateMap = getInterScheduleSimpleStateMap();
        List<SimpleStateEasyObject> simpleStateEasyList = new ArrayList<>();
        for (Object simpleStateObj : interScheduleSimpleStateMap.values()) {
            if (simpleStateObj instanceof SimpleStateEasyObject simpleStateEasyObject) {
                simpleStateEasyList.add(simpleStateEasyObject);
            } else {
                throw new IllegalStateException("InterSchedulerGPUFirst expected SimpleStateEasy but found " + simpleStateObj.getClass().getSimpleName());
            }
        }

        for (InstanceGroup instanceGroup : instanceGroups) {
            int startID = random.nextInt(simpleStateEasyList.size());
            int i = 0;
            for (; i < simpleStateEasyList.size(); i++) {
                SimpleStateEasyObject simpleStateEasy = simpleStateEasyList.get((startID + i) % simpleStateEasyList.size());
                if (simpleStateEasy.isSuitable(instanceGroup)) {
                    simpleStateEasy.allocateResource(instanceGroup.getCpuSum(),
                                                    instanceGroup.getRamSum(),
                                                    instanceGroup.getStorageSum(),
                                                    instanceGroup.getBwSum(),
                                                    instanceGroup.getGpuSum(),
                                                    instanceGroup.getGpuType());
                    interSchedulerResult.addDcResult(instanceGroup, simpleStateEasy.getDatacenter());
                    break;
                }
            }

            if (i == simpleStateEasyList.size()) {
                interSchedulerResult.addFailedInstanceGroup(instanceGroup);
                LOGGER.warn("{}: InterSchedulerRandom failed to schedule InstanceGroup {}", getSimulation().clockStr(), instanceGroup.getId());
            }
        }

        return interSchedulerResult;
    }
}
