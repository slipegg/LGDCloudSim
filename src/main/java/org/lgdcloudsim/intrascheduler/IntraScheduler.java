package org.lgdcloudsim.intrascheduler;

import org.lgdcloudsim.core.DatacenterEntity;
import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.loadbalancer.LoadBalancer;
import org.lgdcloudsim.request.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The intra-scheduler interface.
 * It is used to schedule the instances in the data center.
 * Multi intra-schedulers can coexist in the data center.
 * They are able to schedule the instances in parallel.
 * The {@link LoadBalancer} will distribute the received instances to the intra-schedulers.
 * Like the {@link org.lgdcloudsim.interscheduler.InterScheduler}, each intra-scheduler contains two similar instance queues.
 * One is the new instance queue, and the other is the retry instance queue.
 * Every intra-scheduler has its scheduling view through synchronization.
 * Each intra-scheduler obtains the host state view through synchronization,
 * and the scheduler schedules instances to the host based on its own state view.
 * The scheduling time is also tracked.
 * The intra-scheduler synchronizes the state of the host in the data center through partition synchronization.
 * Every intra-scheduler has the first synchronization partition id.
 * The intra-scheduler will synchronize the host states of a partition every partition synchronization,
 * starting from the first synchronization partition and continuing in a loop.
 * The num of intra-schedulers in the data center need to less than or equal to the num of partitions in the data center.
 * If you want to customize an intra-scheduler,
 * we do not recommend that you directly implement this interface.
 * Instead, we recommend that you directly extend {@link IntraSchedulerSimple},
 * and implement the key scheduling functions, including {@link IntraSchedulerSimple#scheduleInstances}.
 * The {@link IntraSchedulerLeastRequested} may be a good example for you to refer to.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface IntraScheduler extends Nameable, DatacenterEntity {
    /**
     * The Logger of the class.
     */
    Logger LOGGER = LoggerFactory.getLogger(IntraScheduler.class.getSimpleName());

    /**
     * Set the name of the intra-scheduler.
     *
     * @param name the name of the intra-scheduler.
     * @return the intra-scheduler itself.
     */
    IntraScheduler setName(String name);

    /**
     * Add the instance to the instance queue.
     * If the flag isRetry is true, the instance will be added to the retry instance queue.
     * Otherwise, the instance will be added to the new instance queue.
     * @param instances the instances to be added to the instance queue.
     * @param isRetry whether the instance is added to the retry instance queue.
     * @return the intra-scheduler itself.
     */
    IntraScheduler addInstance(List<Instance> instances, boolean isRetry);

    /**
     * Get whether the new instance queue and the retry instance queue are empty.
     * @return the new instance queue size.
     */
    boolean isQueuesEmpty();

    /**
     * Get the new instance queue size.
     * @return the new instance queue size.
     */
    int getNewInstanceQueueSize();

    /**
     * Get the retry instance queue size.
     * @return the retry instance queue size.
     */
    int getRetryInstanceQueueSize();

    /**
     * Schedule the instances in the instance queue to the host.
     * @return the result of the scheduling.
     */
    IntraSchedulerResult schedule();

    /**
     * Get the time spent on the scheduling.
     * @return the time spent on the scheduling.
     */
    double getScheduleCostTime();

    /**
     * Set the time spent on the scheduling.
     * @param scheduleCostTime the time spent on the scheduling.
     * @return the intra-scheduler itself.
     */
    IntraScheduler setScheduleCostTime(double scheduleCostTime);

    /**
     * Set the first synchronization partition id of the intra-scheduler.
     * @param firstPartitionId the first synchronization partition id of the intra-scheduler.
     * @return the intra-scheduler itself.
     */
    IntraScheduler setFirstPartitionId(int firstPartitionId);

    /**
     * Get the first synchronization partition id of the intra-scheduler.
     * @return the first synchronization partition id of the intra-scheduler.
     */
    int getFirstPartitionId();
}
