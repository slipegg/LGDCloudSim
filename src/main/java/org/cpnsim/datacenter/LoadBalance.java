package org.cpnsim.datacenter;

import org.cloudsimplus.core.DatacenterEntity;
import org.cpnsim.request.Instance;
import org.cpnsim.innerscheduler.InnerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * An interface to be implemented by each class that represents a load balancer.
 * After the {@link Instance} arrives at the datacenter,
 * it is necessary to allocate each instance to each {@link InnerScheduler} through it.
 *
 * @author Jiawen Liu
 * @since CPNSim 1.0
 */
public interface LoadBalance extends DatacenterEntity {
    /**
     * The logger.
     */
    Logger LOGGER = LoggerFactory.getLogger(LoadBalance.class.getSimpleName());

    /**
     * Send instances to the datacenter.
     *
     * @param instances the instance to be sent to the datacenter
     * @return
     */
    List<InnerScheduler> sendInstances(List<Instance> instances);

    /**
     * Set the load balance cost time.
     *
     * @param loadBalanceCostTime the load balance cost time
     */
    LoadBalance setLoadBalanceCostTime(double loadBalanceCostTime);

    /**
     * Get the load balance cost time.
     *
     * @return the load balance cost time
     */
    double getLoadBalanceCostTime();
}
