package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.Instance;
import org.cpnsim.innerscheduler.InnerScheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to represent a load balancer.
 * This load balancer performs load balancing through cyclic allocation
 * This class implements the interface {@link LoadBalance}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class LoadBalanceRound implements LoadBalance {
    /**
     * the datacenter to be load balanced.
     **/
    @Getter
    Datacenter datacenter;

    /**
     * the load balance cost time.
     **/
    @Getter
    @Setter
    double loadBalanceCostTime = 0.1;

    /**
     * the last inner scheduler id.
     **/
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
            innerScheduler.addInstance(instances.subList(start, end), false);
            sentInnerSchedulers.add(innerScheduler);
            start = end;
        }

        lastInnerSchedulerId = (lastInnerSchedulerId + 1) % datacenter.getInnerSchedulers().size();
        LOGGER.info("{}: {}'s LoadBalanceRound send {} instances to {} innerSchedulers,On average, each scheduler receives around {} instances", datacenter.getSimulation().clockStr(), datacenter.getName(), instances.size(), sentInnerSchedulers.size(), onceSendSize);
        return sentInnerSchedulers;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
    }
}
