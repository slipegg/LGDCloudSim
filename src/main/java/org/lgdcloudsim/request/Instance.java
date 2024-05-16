package org.lgdcloudsim.request;

import java.util.List;

/**
 * Instances run on the host, and each instance occupies the host's resources, including CPU, memory, storage, and bandwidth.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

public interface Instance extends RequestEntity {
    /**
     * Get the CPU required by the instance to run on the host. The default unit is Core.
     *
     * @return the CPU required by the instance.
     */
    int getCpu();

    /**
     * Set the CPU required by the instance to run on the host. The default unit is Core.
     *
     * @param cpu the CPU required by the instance.
     * @return the instance itself.
     */
    Instance setCpu(int cpu);

    /**
     * Get the memory required by the instance to run on the host. The default unit is GB.
     * @return the memory required by the instance.
     */
    int getRam();

    /**
     * Set the memory required by the instance to run on the host. The default unit is GB.
     *
     * @param ram the memory required by the instance to run on the host.
     * @return the instance itself.
     */
    Instance setRam(int ram);

    /**
     * Get the storage required by the instance to run on the host. The default unit is GB.
     * @return the storage required by the instance to run on the host.
     */
    int getStorage();

    /**
     * Set the storage required by the instance to run on the host. The default unit is GB.
     *
     * @param storage the storage occupied by the instance running on the host
     * @return the instance itself
     */
    Instance setStorage(int storage);

    /**
     * Get the bandwidth required by the instance running on the host. The default unit is GB.
     * @return the bandwidth required by the instance running on the host.
     */
    int getBw();

    /**
     * Set the bandwidth required by the instance running on the host. The default unit is Mbps.
     * @param bw the bandwidth required by the instance running on the host. The default unit is Mbps.
     * @return the instance itself
     */
    Instance setBw(int bw);

    /**
     * Get the lifecycle of the instance running on the host. The unit is milliseconds.
     * @return the lifecycle of the instance.
     */
    int getLifecycle();

    /**
     * Set the lifecycle of the instance running on the host.The unit is milliseconds.
     * If the lifecycle is set to -1, the instance will never finish running.
     *
     * @param lifecycle the lifecycle of the instance.
     * @return the instance itself.
     */
    Instance setLifecycle(int lifecycle);

    /**
     * Get the instance group to which the instance belongs.
     * @return the instance group to which the instance belongs.
     */
    InstanceGroup getInstanceGroup();

    /**
     * Set the instance group to which the instance belongs.
     * @param instanceGroup the instance group to which the instance belongs
     * @return the instance itself
     */
    Instance setInstanceGroup(InstanceGroup instanceGroup);


    /**
     * You can specify the scheduled hostId for the instance in advance.
     * If it is specified, the system does not need to find the host to be scheduled for the instance.
     * Get whether the hostId to which the instance is to be scheduled is specified.
     * @return whether the hostId to which the instance is to be scheduled is specified
     */
    boolean isSetDestHost();

    /**
     * Set the hostId to which the instance is to be scheduled.
     *
     * @param destHost the host to which the instance is to be scheduled
     * @return the host to which the instance is to be scheduled
     */
    Instance setDestHostId(int destHost);

    /**
     * Get the hostId to which the instance is to be scheduled.
     *
     * @return the hostId to which the instance is to be scheduled
     */
    int getDestHostId();

    /**
     * Get the hostId on which the instance is running.
     * @return the hostId on which the instance is running.
     */
    int getHost();

    /**
     * Set the hostId on which the instance is running.
     * @param host the hostId on which the instance is running.
     * @return the instance itself.
     */
    Instance setHost(int host);

    /**
     * Get the time when the instance started running,
     * which is also the time when the instance was scheduled to the host.
     * The unit is milliseconds.
     * @return the time when the instance started running.
     */
    double getStartTime();

    /**
     * Set the time when the instance started running. The unit is milliseconds.
     * @param startTime the time when the instance started running.
     * @return the instance itself.
     */
    Instance setStartTime(double startTime);

    /**
     * Get the time when the instance finished running. The unit is milliseconds.
     * @return the time when the instance finished running.
     */
    double getFinishTime();

    /**
     * Set the time when the instance finished running. The unit is milliseconds.
     * @param finishTime the time when the instance finished running.
     * @return the instance itself.
     */
    Instance setFinishTime(double finishTime);

    /**
     * Get the maximum number of retries for the instance.
     *
     * @return the maximum number of retries for the instance.
     */
    int getRetryMaxNum();

    /**
     * Set the maximum number of retries for the instance.
     *
     * @param retryMaxNum the maximum number of retries for the instance.
     * @return the instance itself.
     */
    Instance setRetryMaxNum(int retryMaxNum);

    /**
     * Add the number of retries for the instance.
     * If the number of retries exceeds the maximum number of retries, the instance is marked as failed.
     * Eg:
     *      If the maximum number of retries is 0, the instance is marked as failed after the first scheduling failure.
     *      If the maximum number of retries is 3, the instance is marked as failed after 3 retries.
     *      It means that the instance has been scheduled 4 times.
     * @return the instance itself.
     */
    Instance addRetryNum();

    /**
     * Get the number of retries for the instance.
     * @return the number of retries for the instance.
     */
    int getRetryNum();

    /**
     * Set the number of retries for the instance directly.
     * @param retryNum the number of retries for the instance.
     * @return the instance itself.
     */
    Instance setRetryNum(int retryNum);

    /**
     * Add the hostId to the list of hostIds that the instance has retried.
     * So that we can record the hostIds that the instance has retried.
     * @param hostId the hostId to be added to the list of hostIds that the instance has retried.
     * @return the instance itself.
     */
    Instance addRetryHostId(int hostId);

    /**
     * Get the list of hostIds that the instance has retried.
     * @return the list of hostIds that the instance has retried.
     */
    List<Integer> getRetryHostIds();

    /**
     * Get whether the instance is marked as failed.
     *
     * @return whether the instance is marked as failed.
     */
    boolean isFailed();

    /**
     * Get the state of the instance.
     *
     * @return the state of the instance.
     * @see UserRequest#WAITING UserRequest#FAILED UserRequest#SCHEDULING UserRequest#SUCCESS UserRequest#RUNNING
     */
    int getState();

    /**
     * Set the state of the instance.
     *
     * @param state the state of the instance.
     * @return the instance itself.
     * @see UserRequest#WAITING UserRequest#FAILED UserRequest#SCHEDULING UserRequest#SUCCESS UserRequest#RUNNING
     */
    Instance setState(int state);

    /**
     * The scheduled hostId for the instance after intra-scheduling or inter-scheduling is specified in the expectedScheduleHostId.
     * @return the user request to which the instance belongs.
     */
    int getExpectedScheduleHostId();

    /**
     * Set the scheduled hostId for the instance after intra-scheduling or inter-scheduling.
     * @param expectedScheduleHostId the scheduled hostId for the instance after intra-scheduling or inter-scheduling.
     * @return the instance itself.
     */
    Instance setExpectedScheduleHostId(int expectedScheduleHostId);

    /**
     * Get the intraScheduleEndTime of the instance.
     * @return the intraScheduleEndTime of the instance.
     */
    double getIntraScheduleEndTime();

    /**
     * Set the intraScheduleEndTime of the instance.
     * @param intraScheduleEndTime the intraScheduleEndTime of the instance.
     */
    Instance setIntraScheduleEndTime(double intraScheduleEndTime);
}
