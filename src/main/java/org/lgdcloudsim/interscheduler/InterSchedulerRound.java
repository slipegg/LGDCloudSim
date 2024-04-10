package org.lgdcloudsim.interscheduler;

import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.request.InstanceGroup;

import java.util.List;

/**
 * The round-robin inter-scheduler.
 * It is extended from the {@link InterSchedulerSimple}.
 * It changes the scheduleToDatacenter function to implement the round-robin scheduling strategy.
 * The scheduled data center is the next of last scheduled data center.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class InterSchedulerRound extends InterSchedulerSimple {
    /**
     * The index of the last scheduled data center.
     */
    private int lastSendDCIndex = 0;

    /**
     * The constructor of the round-robin inter-scheduler.
     *
     * @param id               the id of the inter-scheduler
     * @param simulation       the simulation
     * @param collaborationId  the collaboration id
     * @param target           the target of the inter-scheduler
     * @param isSupportForward whether the scheduled instance group results support forward again
     */
    public InterSchedulerRound(int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        super(id, simulation, collaborationId, target, isSupportForward);
    }

    /**
     * Schedule the instance groups to the data center.
     * It implements the round-robin scheduling strategy.
     * The scheduled data center is the next of last scheduled data center.
     *
     * @param instanceGroups the instance groups to be scheduled
     * @return the result of the scheduling
     */
    @Override
    protected InterSchedulerResult scheduleToDatacenter(List<InstanceGroup> instanceGroups) {
        List<Datacenter> allDatacenters = simulation.getCollaborationManager().getDatacenters(collaborationId);
        InterSchedulerResult interSchedulerResult = new InterSchedulerResult(this, allDatacenters);
        lastSendDCIndex = (lastSendDCIndex + 1) % allDatacenters.size();
        Datacenter targetDC = allDatacenters.get(lastSendDCIndex);
        for (InstanceGroup instanceGroup : instanceGroups) {
            interSchedulerResult.addDcResult(instanceGroup, targetDC);
        }
        return interSchedulerResult;
    }
}
