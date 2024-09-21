package org.lgdcloudsim.loadbalancer;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.datacenter.Datacenter;

import java.util.*;

/**
 * A class to represent a load balancer.
 * This load balancer performs load balancing through cyclic allocation
 * This class implements the interface {@link LoadBalancer}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class LoadBalancerRound<R, S> implements LoadBalancer<R, S> {

    /**
     * the load balance cost time.
     **/
    @Getter
    @Setter
    double loadBalanceCostTime = 0.1;

    /**
     * the last distributed id.
     **/
    int lastDistributedId = 0;

    /**
     * Overrides the method to send instances to intra schedulers or send instanceGroups to inter-schedulers.
     * The method divides all instances into fractions of the number of schedulers,
     * and then distributes them to each scheduler.
     *
     * @param requests List of instances or instanceGroups to be sent.
     * @param schedulers List of intra-schedulers or inter-schedulers to which instances are sent.
     * @return The result of the distribution. The key is the intra-scheduler or inter-scheduler, and the value is the instance or instanceGroup to be sent.
     */
    @Override
    public Map<S, List<R>> loadBalance(List<R> requests, List<S> schedulers) {
        Map<S, List<R>> resultMap = new HashMap<>();
        int size = requests.size();
        int onceSendSize = size / schedulers.size();
        int remainder = size % schedulers.size();

        int start = 0;
        int end;
        for (int i = 0; i < schedulers.size(); i++) {
            S scheduler = schedulers.get((lastDistributedId + i) % schedulers.size());
            end = start + onceSendSize + (i < remainder ? 1 : 0);
            if (end == start) { // no more requests need to be distributed
                break;
            }

            resultMap.put(scheduler, requests.subList(start, end));
            start = end;
        }
        lastDistributedId = (lastDistributedId + 1) % schedulers.size();

        return resultMap;
    }
}
