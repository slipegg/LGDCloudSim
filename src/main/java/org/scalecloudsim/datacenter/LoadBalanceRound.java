package org.scalecloudsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.innerscheduler.InnerScheduler;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.math.NumberUtils.max;

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
        List<InnerScheduler> sentInnerSchedulers = new ArrayList<>();
        int size = instances.size();
        List<InnerScheduler> innerSchedulers = datacenter.getInnerSchedulers();
        int onceSendSize = size / innerSchedulers.size();
        int remainder = size % innerSchedulers.size();

        int start = 0;
        int end;
        for (int i = 0; i < innerSchedulers.size(); i++) {
            InnerScheduler innerScheduler = innerSchedulers.get((lastInnerSchedulerId + i) % innerSchedulers.size());
            end = start + onceSendSize + (i < remainder ? 1 : 0);
            if (end == start) {
                break;
            }
            innerScheduler.addInstance(instances.subList(start, end));
            sentInnerSchedulers.add(innerScheduler);
            start = end;
        }

        lastInnerSchedulerId = (lastInnerSchedulerId + 1) % datacenter.getInnerSchedulers().size();
        LOGGER.info("{}: {}'s LoadBalanceRound send {} instances to {} innerSchedulers,On average, each scheduler receives around {} instances", datacenter.getSimulation().clockStr(), datacenter.getName(), instances.size(), sentInnerSchedulers.size(), onceSendSize);
        return sentInnerSchedulers;
    }
}
