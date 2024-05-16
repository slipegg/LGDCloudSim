package org.lgdcloudsim.request;

import org.lgdcloudsim.datacenter.Datacenter;

import java.util.List;

/**
 * InstanceGroup is a group of instances that are scheduled to run on the same datacenter.
 * It records some basic information about the instances it manages.
 * If the instance group is an affinity group, it may have an access latency limit.
 * It limits the maximum network delay between the area where the user of the instance group belongs
 * and the region of the data center to which the instance group is scheduled.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

public interface InstanceGroup extends RequestEntity {
    /**
     * Get the instances in the instance group.
     *
     * @return the instances in the instance group.
     */
    List<Instance> getInstances();

    /**
     * Set the instances in the instance group.
     * @param instanceList the instances in the instance group.
     * @return the instance group itself.
     */
    InstanceGroup setInstances(List<Instance> instanceList);

    /**
     * We can initially set the ID of the target data center to which the instance group needs to be scheduled.
     * If it is set, the system does not need to find the data center to be scheduled for the instance group.
     * Get whether the target data center to which the instance group needs to be scheduled is set.
     *
     * @return whether the target data center to which the instance group needs to be scheduled is set.
     */
    boolean isSetDestDatacenter();

    /**
     * Get the ID of the target data center to which the instance group needs to be scheduled if it is set initially.
     * @return the ID of the target data center to which the instance group needs to be scheduled if it is set initially.
     */
    int getDestDatacenterId();

    /**
     * Set the ID of the target data center to which the instance group needs to be scheduled.
     * @param destDatacenterId the ID of the target data center to which the instance group needs to be scheduled.
     * @return the instance group itself.
     */
    InstanceGroup setDestDatacenterId(int destDatacenterId);

    /**
     * Get the access latency limit of the instance group.
     * @return the access latency limit of the instance group.
     */
    double getAccessLatency();

    /**
     * Set the access latency limit of the instance group.
     * @param latency the access latency limit of the instance group.
     * @return the instance group itself.
     */
    InstanceGroup setAccessLatency(double latency);

    /**
     * Get the sum of the CPU required by the instances in the instance group.
     *
     * @return the sum of the CPU required by the instances in the instance group.
     */
    long getCpuSum();

    /**
     * Get the sum of the memory required by the instances in the instance group.
     *
     * @return the sum of the memory required by the instances in the instance group.
     */
    long getRamSum();

    /**
     * Get the sum of the storage required by the instances in the instance group.
     * @return the sum of the storage required by the instances in the instance group.
     */
    long getStorageSum();

    /**
     * Get the sum of the bandwidth required by the instances in the instance group.
     * @return the sum of the bandwidth required by the instances in the instance group.
     */
    long getBwSum();

    /**
     * Get the maximum number of retries for the instance group.
     * @return the maximum number of retries for the instance group.
     */
    int getRetryMaxNum();

    /**
     * Set the maximum number of retries for the instance group.
     *
     * @param retryMaxNum the maximum number of retries for the instance group.
     * @return the instance group itself.
     */
    InstanceGroup setRetryMaxNum(int retryMaxNum);

    /**
     * Add the number of retries for the instance group.
     * If the number of retries exceeds the maximum number of retries, the instance group is marked as failed.
     * @see Instance#addRetryNum() They are similar.
     * @return the instance group itself.
     */
    InstanceGroup addRetryNum();

    /**
     * Get the number of retries for the instance group.
     * @return the number of retries for the instance group.
     */
    int getRetryNum();

    /**
     * Set the number of retries for the instance group.
     *
     * @param retryNum the number of retries for the instance group.
     * @return the instance group itself.
     */
    InstanceGroup setRetryNum(int retryNum);

    /**
     * Get the state of the instance group.
     * @return the state of the instance group.
     */
    int getState();

    /**
     * Set the state of the instance group.
     * @param state the state of the instance group.
     * @return the instance group itself.
     */
    InstanceGroup setState(int state);

    /**
     * Get whether the instance group is marked as failed which means the state of the instance group is {@link UserRequest#FAILED}.
     * @return whether the instance group is marked as failed.
     */
    boolean isFailed();

    /**
     * Get the data center to which the instance group is scheduled.
     * @return the data center to which the instance group is scheduled.
     */
    Datacenter getReceiveDatacenter();

    /**
     * Set the data center to which the instance group is scheduled after it is received by the data center.
     * @param receiveDatacenter the data center to which the instance group is scheduled.
     * @return the instance group itself.
     */
    InstanceGroup setReceiveDatacenter(Datacenter receiveDatacenter);

    /**
     * Add the number of instances that have been successfully scheduled.
     * If all the instances in the instance group have been successfully scheduled, the instance group is marked as success.
     * @return the instance group itself.
     */
    InstanceGroup addSuccessInstanceNum();

    /**
     * Get the time when the instance group is received by the data center.
     * @return the time when the instance group is received by the data center.
     */
    double getReceivedTime();

    /**
     * Set the time when the instance group is received by the data center.
     *
     * @param receivedTime the time when the instance group is received by the data center.
     * @return the instance group itself.
     */
    InstanceGroup setReceivedTime(double receivedTime);

    /**
     * Get the time when the instance group is finished.
     * @return the time when the instance group is finished.
     */
    double getFinishTime();

    /**
     * Set the time when the instance group is finished.
     *
     * @param finishedTime the time when the instance group is finished.
     * @return the instance group itself.
     */
    InstanceGroup setFinishTime(double finishedTime);

    /**
     * Add the data center ID to which the instance group has been forwarded.
     * @param datacenterId the data center ID to which the instance group has been forwarded.
     * @return the instance group itself.
     */
    InstanceGroup addForwardDatacenterIdHistory(int datacenterId);

    /**
     * Get the history of the data center IDs to which the instance group has been forwarded.
     * @return the history of the data center IDs to which the instance group has been forwarded.
     */
    List<Integer> getForwardDatacenterIdsHistory();

    /**
     * Get whether the instance group has network limitations,
     * that is, whether there are access delay limitations,
     * and whether there are limitations on renting bandwidth and network connection delays with other instance groups.
     * @return whether the instance group has network restrictions.
     */
    boolean isNetworkLimited();

    /**
     * Get the interScheduleEndTime of the instance group.
     * @return the interScheduleEndTime of the instance group.
     */
    double getInterScheduleEndTime();

    /**
     * Set the interScheduleTime of the instance group.
     * @param interScheduleTime the interScheduleTime of the instance group.
     */
    InstanceGroup setInterScheduleEndTime(double interScheduleTime);
}
