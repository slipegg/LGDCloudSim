package org.scalecloudsim.innerscheduler;

import org.cloudsimplus.core.Nameable;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.datacenters.Datacenter;

import java.util.List;
import java.util.Map;

public interface InnerScheduler extends Nameable {
    InnerScheduler setDatacenter(Datacenter datacenter);

    Datacenter getDatacenter();

    InnerScheduler setPartitionDelay(Map<Integer, Double> partitionDelay);

    Map<Integer, Double> getPartitionDelay();

    InnerScheduler setName(String name);

    InnerScheduler addInstance(List<Instance> instances);

    InnerScheduler addInstance(Instance instance);

    boolean isQueueEmpty();

    int queueSize();

    InnerScheduler schedule();
}
