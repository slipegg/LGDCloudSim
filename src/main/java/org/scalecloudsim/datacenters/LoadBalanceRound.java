package org.scalecloudsim.datacenters;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.innerscheduler.InnerScheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LoadBalanceRound implements LoadBalance {
    @Getter
    @Setter
    Datacenter datacenter;
    int lastInnerSchedulerId = 0;

    @Override
    public void sendInstance(List<Instance> instances) {
        InnerScheduler innerScheduler = datacenter.getInnerSchedulers().get(lastInnerSchedulerId);
        innerScheduler.addInstance(instances);
        lastInnerSchedulerId = (lastInnerSchedulerId + 1) % datacenter.getInnerSchedulers().size();
        LOGGER.info("{}: {}'s LoadBalanceRound send {} instances to {}", datacenter.getSimulation().clockStr(), datacenter.getName(), instances.size(), innerScheduler.getName());
    }
}
