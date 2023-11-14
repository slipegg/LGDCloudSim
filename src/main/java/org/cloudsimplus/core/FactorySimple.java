package org.cloudsimplus.core;

import org.cpnsim.datacenter.*;
import org.cpnsim.innerscheduler.*;
import org.cpnsim.interscheduler.*;
import org.cpnsim.statemanager.PredictionManager;
import org.cpnsim.statemanager.PredictionManagerSimple;

public class FactorySimple implements Factory {
    public InnerScheduler getInnerScheduler(String type, int id, int firstPartitionId, int partitionNum) {
        return switch (type) {
            case "simple", "Simple" -> new InnerSchedulerSimple(id, firstPartitionId, partitionNum);
            case "random" -> new InnerSchedulerRandom(id, firstPartitionId, partitionNum);
            case "partitionRandom" -> new InnerSchedulerPartitionRandom(id, firstPartitionId, partitionNum);
            case "minHostOn" -> new InnerSchedulerMinHostOn(id, firstPartitionId, partitionNum);
            case "FirstFit" -> new InnerSchedulerFirstFit(id, firstPartitionId, partitionNum);
            case "multiLevel" -> new InnerSchedulerPartitionMultiLevel(id, firstPartitionId, partitionNum);
            case "fixedPartitionRandom" -> new InnerSchedulerFixedPartitionRandom(id, firstPartitionId, partitionNum);
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
    public InterScheduler getInterScheduler(String type, int id, Simulation simulation, int collaborationId, boolean isDcTarget, boolean isSupportForward) {
        return switch (type) {
            case "simple", "Simple" ->
                    new InterSchedulerSimple(id, simulation, collaborationId, isDcTarget, isSupportForward);
            case "direct", "Direct" -> new InterSchedulerDirect(id, simulation, collaborationId);
            case "minTCODirect" -> new InterSchedulerMinTCODirect(id, simulation, collaborationId);
            case "consult", "Consult" -> new InterSchedulerConsult(id, simulation, collaborationId);
            default -> null;
        };
    }

    @Override
    public LoadBalance getLoadBalance(String type) {
        return switch (type) {
            case "round", "Round" -> new LoadBalanceRound();
            case "batch", "Batch" -> new LoadBalanceBatch();
            default -> null;
        };
    }

    @Override
    public ResourceAllocateSelector getResourceAllocateSelector(String type) {
        return switch (type) {
            case "simple", "Simple" -> new ResourceAllocateSelectorSimple();
            default -> null;
        };
    }
}
