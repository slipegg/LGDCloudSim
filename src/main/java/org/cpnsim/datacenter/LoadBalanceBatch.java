package org.cpnsim.datacenter;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.cpnsim.request.Instance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A class to represent a load balancer.
 * This load balancer performs load balancing through cyclic allocation
 * This class implements the interface {@link LoadBalance}.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public class LoadBalanceBatch implements LoadBalance {
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
    public Set<InnerScheduler> sendInstances(List<Instance> instances) {
        Set<InnerScheduler> sentInnerSchedulers = new HashSet<>();
        int batchSize = 100;
        int size = instances.size();
        int startIndex = 0;
        int endIndex = 0;
        while (endIndex < size) {
            endIndex = Math.min(startIndex + batchSize, size);
            List<Instance> batchInstances = instances.subList(startIndex, endIndex);
            InnerScheduler innerScheduler = datacenter.getInnerSchedulers().get(lastInnerSchedulerId);
            lastInnerSchedulerId = (lastInnerSchedulerId + 1) % datacenter.getInnerSchedulers().size();
            innerScheduler.addInstance(batchInstances, false);
            sentInnerSchedulers.add(innerScheduler);
            startIndex = endIndex;
        }

        LOGGER.info("{}: {}'s LoadBalanceRound send {} instances to {} innerSchedulers,On average, each scheduler receives around {} instances",
                datacenter.getSimulation().clockStr(), datacenter.getName(), instances.size(),
                sentInnerSchedulers.size(), instances.size() / sentInnerSchedulers.size());
        return sentInnerSchedulers;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
    }
}
