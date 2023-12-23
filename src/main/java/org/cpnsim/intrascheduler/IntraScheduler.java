package org.cpnsim.intrascheduler;

import org.cpnsim.core.DatacenterEntity;
import org.cpnsim.core.Nameable;
import org.cpnsim.datacenter.InstanceQueue;
import org.cpnsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface IntraScheduler extends Nameable, DatacenterEntity {
    Logger LOGGER = LoggerFactory.getLogger(IntraScheduler.class.getSimpleName());

    IntraScheduler setPartitionDelay(Map<Integer, Double> partitionDelay);

    Map<Integer, Double> getPartitionDelay();

    IntraScheduler setName(String name);

    IntraScheduler addInstance(List<Instance> instances, boolean isRetry);

    IntraScheduler addInstance(Instance instance, boolean isRetry);

    boolean isQueuesEmpty();

    int getNewInstanceQueueSize();

    int getRetryInstanceQueueSize();

    IntraSchedulerResult schedule();

    double getScheduleCostTime();

    IntraScheduler setScheduleCostTime(double scheduleCostTime);

    IntraScheduler setFirstPartitionId(int firstPartitionId);

    int getFirstPartitionId();

    double getLastScheduleTime();

    InstanceQueue getInstanceQueue();
}
