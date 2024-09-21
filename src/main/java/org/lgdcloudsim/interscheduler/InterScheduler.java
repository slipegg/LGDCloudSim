package org.lgdcloudsim.interscheduler;

import org.lgdcloudsim.core.DatacenterEntity;
import org.lgdcloudsim.core.Nameable;
import org.lgdcloudsim.core.Simulation;
import org.lgdcloudsim.datacenter.Datacenter;
import org.lgdcloudsim.queue.InstanceGroupQueue;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.request.UserRequest;

import java.util.List;
import java.util.Map;

/**
 * It is an interface for the inter-scheduler.
 * The inter-scheduler can exist in the {@link org.lgdcloudsim.core.CloudInformationService} as the only centralized inter-scheduler for each collaboration area.
 * It can also exist in each data center as a distributed inter-scheduler.
 * In the current design, only one centralized inter-scheduler can exist in each collaboration zone, and only one inter-scheduler can exist in each data center.
 * TODO We will add support for multiple inter-scheduler parallel scheduling architectures in the near future.
 * The basic scheduling unit of the inter-scheduler is the {@link InstanceGroup}.
 * Each inter-scheduler maintains two {@link InstanceGroupQueue} of instance groups:
 * one for normally scheduled groups and another for groups awaiting rescheduling due to previous scheduling failures.
 * The inter-scheduler synchronously acquires the customizable state of various data centers from each data center’s {@link org.lgdcloudsim.statemanager.StatesManager},
 * like the total remaining resources of each data center.
 * The data content obtained synchronously is determined by {@link InterScheduler#setDcStateSynType}.
 * If the synchronization interval is set to 0, it means real-time synchronization.
 * The synchronization interval is determined by {@link InterScheduler#setDcStateSynInterval}.
 * Scheduling will take time in simulation, you can get it through {@link InterScheduler#getScheduleTime}, the default is to count the real scheduling time.
 * During scheduling, the scheduling will be completed at the start time of the scheduling{@link org.lgdcloudsim.core.CloudActionTags#INTER_SCHEDULE_BEGIN},
 * and the time spent on the scheduling will be counted,
 * and then the scheduling end event{@link org.lgdcloudsim.core.CloudActionTags#INTER_SCHEDULE_END} will be sent.
 * The delay in the occurrence of the scheduling end event{@link org.lgdcloudsim.core.CloudActionTags#INTER_SCHEDULE_END} is the time spent on the scheduling just counted.
 *
 * The scheduler has three scheduling types depending on the scheduling target now:
 * <ul>
 *     <li>The scheduling target is a host in the data center: It is implemented by the {@link InterSchedulerSimple#scheduleToHost} function.
 *     It not only determines the data center to which each instance group will be scheduled,
 *     but also determines the hosts in the data center to which each instance in the instance group will be scheduled.
 *     </li>
 *     <li>The scheduling target is a data center: It is implemented by the {@link InterSchedulerSimple#scheduleToDatacenter} function.
 *     It only determines the data center to which each instance group will be scheduled.
 *     There is another key attribute here, which is whether the scheduled instance group is allowed to be forwarded after sent to the target data center.
 *     If forwarding is no longer allowed, it will enter the scheduling process in the data center and be scheduled by {@link org.lgdcloudsim.intrascheduler.IntraScheduler}.
 *     If forwarding is allowed, it will be sent to the target data center and be scheduled by the inter-scheduler in the target data center again.
 *     </li>
 *     <li>The scheduling target is a mix of hosts and data centers: It is implemented by the {@link InterSchedulerSimple#scheduleMixed} function.
 *     It schedules instances in some instance groups to hosts in this data center, and forwards the remaining instance groups to other data centers.
 *     It also has the attribute of whether the scheduled instance group is allowed to be forwarded after sent to the target data center.
 *     </li>
 * </ul>
 *
 * Different types of inter-schedulers are suitable for use in different types of inter-data center scheduling architectures.
 * The host-targeted inter-scheduler is suitable for use in the centralized inter-scheduler of the collaboration zone in CIS(@link org.lgdcloudsim.core.CloudInformationService).
 * The data center-targeted-no-forwarding inter-scheduler is suitable for use in the centralized inter-scheduler of the collaboration zone in CIS(@link org.lgdcloudsim.core.CloudInformationService).
 * It means that after the centralized inter-scheduler distributes the request to each data center, it is further scheduled by the {@link org.lgdcloudsim.intrascheduler.IntraScheduler} in the data center.
 * The data center-targeted-forwarding inter-scheduler is alse suitable for use in the centralized inter-scheduler of the collaboration zone in CIS(@link org.lgdcloudsim.core.CloudInformationService).
 * It means that after the centralized inter-scheduler distributes the request to each data center, it is further scheduled by the inter-scheduler in the target data center.
 * The mixed-targeted inter-scheduler is suitable for use in the distributed inter-scheduler in each data center.
 * It is used to schedule instance group scheduled from the centralized data center-targeted-forwarding inter-scheduler.
 * It also can be used in no-centralized inter-scheduler scheduling architecture,
 * then it can schedule the instance group received to other data centers or hosts in its own data center.
 *
 * If you need to customize an inter-scheduler, we do not recommend that you directly implement interScheduler.
 * Instead, we recommend that you directly extend {@link InterSchedulerSimple} and then implement several key scheduling functions,
 * including {@link InterSchedulerSimple#scheduleToHost}, {@link InterSchedulerSimple#scheduleToDatacenter}, and {@link InterSchedulerSimple#scheduleMixed}.
 * The {@link InterSchedulerLeastRequested} may be a good example for you to refer to.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */
public interface InterScheduler extends Nameable, DatacenterEntity {
    /**
     * Get a batch of instance groups from the instance group queue for scheduling.
     * It will call different scheduling functions according to the scheduling target.
     *
     * @return the result of the scheduling.
     */
    InterSchedulerResult schedule();

    /**
     * Get the time spent on the scheduling.
     * @return the time spent on the scheduling.
     */
    double getScheduleTime();

    /**
     * Set the simulation.
     * @param simulation the simulation.
     * @return the inter-scheduler itself.
     */
    InterScheduler setSimulation(Simulation simulation);

    /**
     * Get the collaboration id.
     * @return the collaboration id.
     */
    int getCollaborationId();

    /**
     * Set the collaboration id.
     * It is used for the centralized inter-scheduler of the collaboration zone in CIS.
     * @param collaborationId the collaboration id.
     * @return the inter-scheduler itself.
     */
    InterScheduler setCollaborationId(int collaborationId);

    /**
     * Get the state synchronization interval of each data center.
     * @return the state synchronization interval of each data center.
     */
    Map<Datacenter, Double> getDcStateSynInterval();

    /**
     * Set the state synchronization interval of each data center.
     *
     * @param dcStateSynInterval the state synchronization interval of each data center.
     * @return the inter-scheduler itself.
     */
    InterScheduler setDcStateSynInterval(Map<Datacenter, Double> dcStateSynInterval);

    /**
     * Set the state synchronization type of each data center.
     * Depending on the state type, the {@link org.lgdcloudsim.statemanager.StatesManager} will return different types of state copies.
     *
     * @param dcStateSynType the state synchronization type of each data center.
     * @return the inter-scheduler itself.
     * @see org.lgdcloudsim.statemanager.StatesManager#getStateByType(String)
     */
    InterScheduler setDcStateSynType(Map<Datacenter, String> dcStateSynType);

    /**
     * Get the state synchronization type of each data center.
     * If the synchronization interval to some data centers is 0,
     * it will call this function to get the latest simple state of the data centers.
     * For other data centers whose synchronization period is not 0,
     * it will periodically call this function for synchronization.
     *
     * @param datacenters the data centers.
     */
    void synBetweenDcState(List<Datacenter> datacenters);

    /**
     * Add user requests to the instance group queue.
     * @param userRequests the user requests to be added.
     */
    void addUserRequests(List<UserRequest> userRequests);

    /**
     * Add instance groups to the instance group queue.
     * If the isRetry is true, it means that the instance groups are added for retry instance group queue.
     * @param instanceGroups the instance groups to be added.
     * @param isRetry indicates whether the instance groups are added for retry.
     */
    void addInstanceGroups(List<InstanceGroup> instanceGroups, boolean isRetry);

    /**
     * Get whether the instance group queue is empty.
     * @return whether the instance group queue is empty.
     */
    boolean isQueuesEmpty();

    /**
     * Get the size of the instance group queue for new instance groups.
     * @return the size of the instance group queue for new instance groups.
     */
    int getNewQueueSize();

    /**
     * Get the size of the instance group queue for retry instance groups.
     * @return the size of the instance group queue for retry instance groups.
     */
    int getRetryQueueSize();

    /**
     * Get the number of traversal times.
     * If you don't update the traversal time in your scheduling strategy, it will return 0.
     * @return the number of traversal times.
     */
    int getTraversalTime();

    /**
     * Get the target type of the inter-scheduler.
     * See {@link org.lgdcloudsim.interscheduler.InterSchedulerSimple},
     *
     * @return the target id of the inter-scheduler.
     */
    int getTarget();

    /**
     * Get whether the inter-scheduler's scheduling results support forward.
     *
     * @return whether the inter-scheduler's scheduling results support forward.
     */
    boolean isSupportForward();
}
