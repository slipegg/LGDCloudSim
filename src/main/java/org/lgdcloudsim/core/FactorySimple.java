package org.lgdcloudsim.core;

import org.lgdcloudsim.conflicthandler.ConflictHandler;
import org.lgdcloudsim.conflicthandler.ConflictHandlerSimple;
import org.lgdcloudsim.intrascheduler.*;
import org.lgdcloudsim.interscheduler.*;
import org.lgdcloudsim.loadbalancer.LoadBalancer;
import org.lgdcloudsim.loadbalancer.LoadBalancerBatch;
import org.lgdcloudsim.loadbalancer.LoadBalancerRound;
import org.lgdcloudsim.statemanager.PredictionManager;
import org.lgdcloudsim.statemanager.PredictionManagerSimple;

/**
 * A simple factory that implements the {@link Factory} interface.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class FactorySimple implements Factory {
    @Override
    public IntraScheduler getIntraScheduler(String type, int id, int firstPartitionId, int partitionNum) {
        return switch (type) {
            case "simple", "Simple" -> new IntraSchedulerSimple(id, firstPartitionId, partitionNum);
            case "leastRequested" -> new IntraSchedulerLeastRequested(id, firstPartitionId, partitionNum);
            case "randomScore" -> new IntraSchedulerRandomScore(id, firstPartitionId, partitionNum);
            case "randomScoreByPartitionSynOrder" ->
                    new IntraSchedulerRandomScoreByPartitionSynOrder(id, firstPartitionId, partitionNum);
            case "random" -> new IntraSchedulerRandom(id, firstPartitionId, partitionNum);
            case "FirstFit" -> new IntraSchedulerFirstFit(id, firstPartitionId, partitionNum);
            case "multiLevel" -> new IntraSchedulerPartitionMultiLevel(id, firstPartitionId, partitionNum);
            case "fixedPartitionRandom" -> new IntraSchedulerFixedPartitionRandom(id, firstPartitionId, partitionNum);
            default -> null;
        };
    }

    @Override
    public PredictionManager getPredictionManager(String type) {
        return switch (type) {
            case "simple", "Simple" -> new PredictionManagerSimple();
            default -> null;
        };
    }

    @Override
    public InterScheduler getInterScheduler(String type, int id, Simulation simulation, int collaborationId, int target, boolean isSupportForward) {
        return switch (type) {
            case "simple", "Simple" ->
                    new InterSchedulerSimple(id, simulation, collaborationId, target, isSupportForward);
            case "leastRequested" ->
                    new InterSchedulerLeastRequested(id, simulation, collaborationId, target, isSupportForward);
            case "round" -> new InterSchedulerRound(id, simulation, collaborationId, target, isSupportForward);
            default -> null;
        };
    }

    @Override
    public LoadBalancer getLoadBalance(String type) {
        return switch (type) {
            case "round", "Round" -> new LoadBalancerRound();
            case "batch", "Batch" -> new LoadBalancerBatch();
            default -> null;
        };
    }

    @Override
    public ConflictHandler getResourceAllocateSelector(String type) {
        return switch (type) {
            case "simple", "Simple" -> new ConflictHandlerSimple();
            default -> null;
        };
    }
}
