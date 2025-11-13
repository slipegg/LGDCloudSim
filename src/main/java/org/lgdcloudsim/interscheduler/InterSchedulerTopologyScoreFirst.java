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

public class InterSchedulerTopologyScoreFirst extends InterSchedulerSimple {
        /**
     * The constructor of the clos topology inter-scheduler.
     *
     * @param id               the id of the inter-scheduler.
     * @param simulation       the simulation object.
     * @param collaborationId  the collaboration id of the inter-scheduler.
     * @param target           the target id of the inter-scheduler.
     * @param isSupportForward whether the inter-scheduler supports forward.
     */
    public InterSchedulerTopologyScoreFirst(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
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

        instanceGroups = scheduleForClosTopology(instanceGroups, interSchedulerResult, 2);

        for (InstanceGroup instanceGroup : instanceGroups) {
            interSchedulerResult.addFailedInstanceGroup(instanceGroup);
        }

        return interSchedulerResult;
    }

    private List<InstanceGroup> scheduleForClosTopology(List<InstanceGroup> unscheduledInstanceGroups, InterSchedulerResult interSchedulerResult, int level) {
        List<InstanceGroup> unscheduledInstanceGroup = new ArrayList<>();
        Map<Datacenter, Object> interScheduleSimpleStateMap = getInterScheduleSimpleStateMap();
        List<ClosTopologyStateSimple> closTopologyStateSimpleList = new ArrayList<>();
        for (Object simpleStateObj : interScheduleSimpleStateMap.values()) {
            if (simpleStateObj instanceof DcClosTopologyStateSimple dcClosTopologyStateSimple) {
                closTopologyStateSimpleList.addAll(dcClosTopologyStateSimple.getClosTopologyStateSimpleMap().get(level));
            } else {
                throw new IllegalStateException("InterSchedulerClosTopology expected DcClosTopologyStateSimple but found " + simpleStateObj.getClass().getSimpleName());
            }
        }

        // Sort closTopologyStateSimpleList by topologyScoreSum, then by availableGpuSum, then by availableCpuSum, then by availableRamSum

        for (InstanceGroup instanceGroup : unscheduledInstanceGroups) {
            closTopologyStateSimpleList.sort(Comparator
                .comparingLong(ClosTopologyStateSimple::getTopologyScoreSum)
                .thenComparingLong(ClosTopologyStateSimple::getAvailableGpuSum)
                .thenComparingLong(ClosTopologyStateSimple::getAvailableCpuSum)
                .thenComparingLong(ClosTopologyStateSimple::getAvailableRamSum)
                .thenComparingLong(ClosTopologyStateSimple::getAvailableStorageSum)
                .thenComparingLong(ClosTopologyStateSimple::getAvailableBwSum)
                .reversed());
            // n-1, n-2, n-3, ... n-m
            // n * m - (1+2+3+...+m) = n * m - m * (m + 1) / 2
            boolean isScheduled = false;
            for (ClosTopologyStateSimple closTopologyStateSimple : closTopologyStateSimpleList) {
                if (closTopologyStateSimple.getAvailableGpuSum() >= instanceGroup.getGpuSum()
                    && closTopologyStateSimple.getAvailableCpuSum() >= instanceGroup.getCpuSum()
                    && closTopologyStateSimple.getAvailableRamSum() >= instanceGroup.getRamSum()) {
                    interSchedulerResult.addDcResult(instanceGroup, closTopologyStateSimple.getOriginalDatacenter());
                    isScheduled = true;

                    long avgInstanceGpu = instanceGroup.getGpuSum() / instanceGroup.getInstances().size(); // instanceGroup.getInstances().size() * closTopologyStateSimple.getHostNum()
                    int m = instanceGroup.getInstances().size();
                    int n = (int) closTopologyStateSimple.getHostNum();
                    closTopologyStateSimple.setTopologyScoreSum(closTopologyStateSimple.getTopologyScoreSum() - avgInstanceGpu * (avgInstanceGpu - 1) * (n * m - m * (m + 1) / 2));
                    closTopologyStateSimple.setAvailableGpuSum(closTopologyStateSimple.getAvailableGpuSum() - instanceGroup.getGpuSum());
                    closTopologyStateSimple.setAvailableCpuSum(closTopologyStateSimple.getAvailableCpuSum() - instanceGroup.getCpuSum());
                    closTopologyStateSimple.setAvailableRamSum(closTopologyStateSimple.getAvailableRamSum() - instanceGroup.getRamSum());
                    closTopologyStateSimple.setAvailableStorageSum(closTopologyStateSimple.getAvailableStorageSum() - instanceGroup.getStorageSum());
                    closTopologyStateSimple.setAvailableBwSum(closTopologyStateSimple.getAvailableBwSum() - instanceGroup.getBwSum());
                    
                    LOGGER.info("{}: InterSchedulerTopologyScoreFirst scheduling InstanceGroup {} to Datacenter {} by switch level {}", getSimulation().clockStr(), instanceGroup.getId(), closTopologyStateSimple.getOriginalDatacenter().getId(), level);
                    break;
                }
            }
            if (!isScheduled) {
                unscheduledInstanceGroup.add(instanceGroup);
            }
        }

        return unscheduledInstanceGroup;
    }
}
