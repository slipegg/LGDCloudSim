package org.lgdcloudsim.loadbalancer;

import org.lgdcloudsim.core.DatacenterEntity;
import org.lgdcloudsim.intrascheduler.IntraScheduler;
import org.lgdcloudsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * An interface to be implemented by each class that represents a load balancer.
 * After the {@link Instance} arrives at the datacenter,
 * it is necessary to allocate each instance to each {@link IntraScheduler} through it.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface LoadBalancer extends DatacenterEntity {
    /**
     * The logger of the class.
     */
    Logger LOGGER = LoggerFactory.getLogger(LoadBalancer.class.getSimpleName());

    /**
     * Send instances to the datacenter.
     *
     * @param instances the instance to be sent to the datacenter
     * @return the intra-schedulers that the instances are sent to
     */
    Set<IntraScheduler> sendInstances(List<Instance> instances);

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
