package org.lgdcloudsim.loadbalancer;

import org.lgdcloudsim.core.DatacenterEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * An interface to be implemented by each class that represents a load balancer.
 * Since the data center or CIS can have multiple inter-schedulers,
 * and the pages within the data center can have multiple intra-schedulers,
 * the load balancer needs to uniformly distribute the instance group to each inter-scheduler,
 * or distribute the instance to each intra-scheduler.
 * Specifically, R refers to the request, which can be an instance group or an instance,
 * and S is the scheduler, which can be an inter-scheduler or an intra-scheduler.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface LoadBalancer<R, S> {
    /**
     * Send instances to the datacenter.
     *
     * @param instances the instance to be sent to the datacenter
     * @return the intra-schedulers that the instances are sent to
     */
    Map<S, List<R>> loadBalance(List<R> instances, List<S> schedulers);

    /**
     * Set the load balance cost time.
     *
     * @param loadBalanceCostTime the load balance cost time
     */
    LoadBalancer setLoadBalanceCostTime(double loadBalanceCostTime);

    /**
     * Get the load balance cost time.
     *
     * @return the load balance cost time
     */
    double getLoadBalanceCostTime();
}
