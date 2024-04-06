package org.lgdcloudsim.loadbalancer;

import lombok.Getter;
import lombok.Setter;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.request.Instance;
import org.lgdcloudsim.request.InstanceGroup;

import java.util.*;

/**
 * A class to represent a load balancer.
 * This load balancer performs load balancing by distributing in small batches.
 * This class implements the interface {@link LoadBalancer}.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public class LoadBalancerBatch<R, S> implements LoadBalancer<R, S> {
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
     * This method distributes requests to each scheduler in batches according to the batch size
     * until all requests have been issued.
     *
     * @param requests List of instances or instanceGroups to be sent.
     * @param schedulers List of intra-schedulers or inter-schedulers to which instances are sent.
     * @return The result of the distribution. The key is the intra-scheduler or inter-scheduler, and the value is the instance or instanceGroup to be sent.
     */
    @Override
    public Map<S, List<R>> loadBalance(List<R> requests, List<S> schedulers) {
        Map<S, List<R>> resultMap = new HashMap<>();
        int batchSize = 100;
        int size = requests.size();
        int startIndex = 0;
        int endIndex = 0;
        while (endIndex < size) {
            endIndex = Math.min(startIndex + batchSize, size);
            List<R> batchRequests = requests.subList(startIndex, endIndex);
            S scheduler = schedulers.get(lastDistributedId);
            lastDistributedId = (lastDistributedId + 1) % schedulers.size();

            resultMap.putIfAbsent(scheduler, new ArrayList<>());
            resultMap.get(scheduler).addAll(batchRequests);

            startIndex = endIndex;
        }

        return resultMap;
    }
}
