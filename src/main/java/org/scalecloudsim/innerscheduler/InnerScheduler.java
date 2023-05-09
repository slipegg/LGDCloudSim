package org.scalecloudsim.innerscheduler;

import org.cloudsimplus.core.DatacenterEntity;
import org.cloudsimplus.core.Nameable;
import org.scalecloudsim.request.Instance;
import org.scalecloudsim.datacenter.Datacenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface InnerScheduler extends Nameable, DatacenterEntity {
    Logger LOGGER = LoggerFactory.getLogger(InnerScheduler.class.getSimpleName());

    InnerScheduler setPartitionDelay(Map<Integer, Double> partitionDelay);

    Map<Integer, Double> getPartitionDelay();

    InnerScheduler setName(String name);

    InnerScheduler addInstance(List<Instance> instances);

    InnerScheduler addInstance(Instance instance);

    boolean isQueueEmpty();

    int getQueueSize();

    Map<Integer, List<Instance>> schedule();

    double getScheduleCostTime();

    InnerScheduler setScheduleCostTime(double scheduleCostTime);

    InnerScheduler setFirstPartitionId(int firstPartitionId);

    int getFirstPartitionId();

    double getLastScheduleTime();
}
