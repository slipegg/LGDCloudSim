package org.cloudsimplus.core;

import org.scalecloudsim.datacenter.LoadBalance;
import org.scalecloudsim.datacenter.LoadBalanceRound;
import org.scalecloudsim.datacenter.ResourceAllocateSelector;
import org.scalecloudsim.datacenter.ResourceAllocateSelectorSimple;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.scalecloudsim.innerscheduler.InnerSchedulerSimple;
import org.scalecloudsim.interscheduler.InterScheduler;
import org.scalecloudsim.interscheduler.InterSchedulerSimple;
import org.scalecloudsim.statemanager.PredictionManager;
import org.scalecloudsim.statemanager.PredictionManagerSimple;

import java.util.Map;

public class FactorySimple implements Factory {

    public InnerScheduler getInnerScheduler(String type, int id, int firstPartitionId, int partitionNum) {
        return switch (type) {
            case "simple", "Simple" -> new InnerSchedulerSimple(id, firstPartitionId, partitionNum);
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
