package org.cloudsimplus.core;

import org.cpnsim.datacenter.LoadBalance;
import org.cpnsim.datacenter.LoadBalanceRound;
import org.cpnsim.datacenter.ResourceAllocateSelector;
import org.cpnsim.datacenter.ResourceAllocateSelectorSimple;
import org.cpnsim.innerscheduler.*;
import org.cpnsim.interscheduler.InterScheduler;
import org.cpnsim.interscheduler.InterSchedulerDirect;
import org.cpnsim.interscheduler.InterSchedulerSimple;
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
    public InterScheduler getInterScheduler(String type) {
        return switch (type) {
            case "simple", "Simple" -> new InterSchedulerSimple();
            case "direct", "Direct" -> new InterSchedulerDirect();
            default -> null;
        };
    }

    @Override
    public LoadBalance getLoadBalance(String type) {
        return switch (type) {
            case "round", "Round" -> new LoadBalanceRound();
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
