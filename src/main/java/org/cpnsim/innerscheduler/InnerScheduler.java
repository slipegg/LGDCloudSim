package org.cpnsim.innerscheduler;

import org.cloudsimplus.core.DatacenterEntity;
import org.cloudsimplus.core.Nameable;
import org.cpnsim.datacenter.InstanceQueue;
import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.SynState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface InnerScheduler extends Nameable, DatacenterEntity {
    Logger LOGGER = LoggerFactory.getLogger(InnerScheduler.class.getSimpleName());

    InnerScheduler setPartitionDelay(Map<Integer, Double> partitionDelay);

    Map<Integer, Double> getPartitionDelay();

    InnerScheduler setName(String name);

    InnerScheduler addInstance(List<Instance> instances, boolean isRetry);

    InnerScheduler addInstance(Instance instance, boolean isRetry);

    boolean isQueuesEmpty();

    int getNewInstanceQueueSize();

    int getRetryInstanceQueueSize();

    Map<Integer, List<Instance>> schedule();

    double getScheduleCostTime();

    InnerScheduler setScheduleCostTime(double scheduleCostTime);

    InnerScheduler setFirstPartitionId(int firstPartitionId);

    int getFirstPartitionId();

    double getLastScheduleTime();

    Map<Integer, List<Instance>> scheduleInstances(List<Instance> instances, SynState synState);

    InstanceQueue getInstanceQueue();
}
