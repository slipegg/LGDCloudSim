package org.scalecloudsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.innerscheduler.InnerScheduler;

import java.util.List;

public class LoadBalanceRound implements LoadBalance {
    @Getter
    @Setter
    Datacenter datacenter;

    @Getter
    @Setter
    double loadBalanceCostTime = 0.1;
    int lastInnerSchedulerId = 0;

    @Override
    public List<InnerScheduler> sendInstances(List<Instance> instances) {
        InnerScheduler innerScheduler = datacenter.getInnerSchedulers().get(lastInnerSchedulerId);
        innerScheduler.addInstance(instances);
        lastInnerSchedulerId = (lastInnerSchedulerId + 1) % datacenter.getInnerSchedulers().size();
        LOGGER.info("{}: {}'s LoadBalanceRound send {} instances to {}", datacenter.getSimulation().clockStr(), datacenter.getName(), instances.size(), innerScheduler.getName());
        return List.of(innerScheduler);
    }
}
