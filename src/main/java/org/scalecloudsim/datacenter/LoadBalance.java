package org.scalecloudsim.datacenter;

import org.scalecloudsim.request.Instance;
import org.scalecloudsim.innerscheduler.InnerScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface LoadBalance {
    Logger LOGGER = LoggerFactory.getLogger(LoadBalance.class.getSimpleName());

    LoadBalance setDatacenter(Datacenter datacenter);

    Datacenter getDatacenter();

    List<InnerScheduler> sendInstances(List<Instance> instances);

    LoadBalance setLoadBalanceCostTime(double loadBalanceCostTime);

    double getLoadBalanceCostTime();
}
