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

public class InterSchedulerGPUFirst extends InterSchedulerSimple {
        /**
     * The constructor of the clos topology inter-scheduler.
     *
     * @param id               the id of the inter-scheduler.
     * @param simulation       the simulation object.
     * @param collaborationId  the collaboration id of the inter-scheduler.
     * @param target           the target id of the inter-scheduler.
     * @param isSupportForward whether the inter-scheduler supports forward.
     */
    public InterSchedulerGPUFirst(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
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
        // Sort simpleStateEasyList by availableGpuSum, then by availableCpuSum, then by availableRamSum
        for (InstanceGroup instanceGroup : instanceGroups) {
            simpleStateEasyList.sort(Comparator
                .comparingLong(SimpleStateEasyObject::getGpuAvailableSum)
                .thenComparingLong(SimpleStateEasyObject::getCpuAvailableSum)
                .thenComparingLong(SimpleStateEasyObject::getRamAvailableSum)
                .thenComparingLong(SimpleStateEasyObject::getStorageAvailableSum)
                .thenComparingLong(SimpleStateEasyObject::getBwAvailableSum)
                .reversed());
            boolean isScheduled = false;
            SimpleStateEasyObject simpleStateEasy = simpleStateEasyList.get(0);
            if (simpleStateEasy.getGpuAvailableSum() >= instanceGroup.getGpuSum()
                && simpleStateEasy.getCpuAvailableSum() >= instanceGroup.getCpuSum()
                && simpleStateEasy.getRamAvailableSum() >= instanceGroup.getRamSum()) {
                interSchedulerResult.addDcResult(instanceGroup, simpleStateEasy.getDatacenter());
                isScheduled = true;
                LOGGER.info("{}: InterSchedulerGPUFirst scheduled InstanceGroup {} to Datacenter {}", getSimulation().clockStr(), instanceGroup.getId(), simpleStateEasy.getDatacenter().getId());

                simpleStateEasy.setGpuAvailableSum(simpleStateEasy.getGpuAvailableSum() - instanceGroup.getGpuSum());
                simpleStateEasy.setCpuAvailableSum(simpleStateEasy.getCpuAvailableSum() - instanceGroup.getCpuSum());
                simpleStateEasy.setRamAvailableSum(simpleStateEasy.getRamAvailableSum() - instanceGroup.getRamSum());
                simpleStateEasy.setStorageAvailableSum(simpleStateEasy.getStorageAvailableSum() - instanceGroup.getStorageSum());
                simpleStateEasy.setBwAvailableSum(simpleStateEasy.getBwAvailableSum() - instanceGroup.getBwSum());
            }
            if (!isScheduled) {
                interSchedulerResult.addFailedInstanceGroup(instanceGroup);
                LOGGER.warn("{}: InterSchedulerGPUFirst failed to schedule InstanceGroup {}. The max GpuAvailableSum: {}, CpuAvailableSum: {}, RamAvailableSum: {}, required GpuSum: {}, CpuSum: {}, RamSum: {}.", getSimulation().clockStr(), instanceGroup.getId(), simpleStateEasy.getGpuAvailableSum(), simpleStateEasy.getCpuAvailableSum(), simpleStateEasy.getRamAvailableSum(), instanceGroup.getGpuSum(), instanceGroup.getCpuSum(), instanceGroup.getRamSum());
            }
        }

        return interSchedulerResult;
    }
}
