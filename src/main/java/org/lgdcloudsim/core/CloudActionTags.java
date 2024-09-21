package org.lgdcloudsim.core;

import java.util.Set;

import org.lgdcloudsim.interscheduler.InterSchedulerResult;
import org.lgdcloudsim.loadbalancer.LoadBalancer;

/**
 * Contains various static tags that indicate a type of action that needs to be undertaken
 * by CloudSim entities when they receive or send events.
 * Note that in the same situation, the order in which tags are processed is consistent with the order in which they are declared in the class.
 * 
 * @author Jiawen Liu
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @author Anthony Sulistio
 * @author Remo Andreoli
 * @since LGDCloudsim 1.1
 */
public enum CloudActionTags implements CloudSimTags {
    /**
     * Denotes the base tag value for CloudSim entities.
     * It doesn't represent any specific action.
     */
    NONE,

    /**
     * Denotes a request from a Datacenter to register itself. This tag is normally used
     * between {@link CloudInformationService} and Datacenter entities.
     * When such a {@link org.lgdcloudsim.core.events.SimEvent} is sent, the {@link org.lgdcloudsim.core.events.SimEvent#getData()}
     * must be a {@link org.lgdcloudsim.datacenter.Datacenter} object.
     */
    DC_REGISTRATION_REQUEST,

    /**
     * Periodically modify the scope of each collaboration zone.
     * This tag is used to notify the {@link CloudInformationService} to change the collaboration zone.
     */
    CHANGE_COLLABORATION_SYN,

    /**
     * Denotes the end of the execution of instances.
     * This tag is used by the {@link org.lgdcloudsim.datacenter.Datacenter}.
     * the {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.Instance} objects
     * which are finishing at the same time.
     */
    END_INSTANCE_RUN,

    /**
     * Denotes the end of the execution of instances.
     * This tag is used by the {@link org.lgdcloudsim.datacenter.Datacenter}.
     * the {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.Instance} objects
     * which are finishing at the same time.
     */
    SYN_STATE_BY_HEARTBEAT_IN_DC,

    /**
     * Synchronize host states to intra-scheduler in the data center.
     * It will be executed periodically.
     */
    SYN_STATE_BETWEEN_CENTER_AND_INTRA_SCHEDULER_IN_DC,

    /**
     * Synchronize host states to inter-scheduler between data centers.
     * It will be executed periodically.
     */
    SYN_STATE_BETWEEN_DC,

    /**
     * Denotes that some user request has failed.
     * This event will be sent to {@link CloudInformationService} for processing.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a set of {@link org.lgdcloudsim.request.UserRequest} object.
     */
    USER_REQUEST_FAIL,

    /**
     * Denotes a request from the {@link org.lgdcloudsim.user.UserSimple} to a {@link CloudInformationService} to get
     * the list of all Datacenters which are registered with the CloudInformationService.
     */
    DC_LIST_REQUEST,

    /**
     * Denotes a request from the {@link org.lgdcloudsim.user.UserSimple} to send.
     * It can be sent to the {@link CloudInformationService} or to a {@link org.lgdcloudsim.datacenter.Datacenter}.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.UserRequest} object.
     * It also can be the {@link org.lgdcloudsim.interscheduler.InterScheduler} scheduling result.
     * In this case, the instanceGroups can be forwarded to other data centers.
     */
    USER_REQUEST_SEND,

    /**
     * Denotes that there are still user requests that need to be sent by the {@link org.lgdcloudsim.user.UserSimple}.
     */
    NEED_SEND_USER_REQUEST,

    /**
     * Denotes the start of inter-scheduler scheduling.
     * Note that it must be bigger than {@link #USER_REQUEST_SEND}.
     * Therefore, when two events arrive at the same time,
     * the user request can be placed in the queue first, and then inter-scheduler scheduling can be started.
     */
    INTER_SCHEDULE_BEGIN,

    /**
     * Denotes the end of inter-scheduler scheduling.
     * The event will be sent after the execution of the {@link #INTER_SCHEDULE_BEGIN}.
     * The time interval between two events is the time spent on inter-scheduling.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link InterSchedulerResult} object.
     */
    INTER_SCHEDULE_END,

    /**
     * Send the scheduling results of the {@link org.lgdcloudsim.interscheduler.InterScheduler} to the data center.
     * This type of scheduling only clarifies which data center the instance group is scheduled to
     * and does not perform host-level scheduling on the instances in the instance group.
     * After the instance group is sent to the data center,
     * the data center does not allow the instance group to be forwarded to other data centers.
     * So the datacenter will call intra-scheduler to schedule the instance group.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.InstanceGroup} object.
     */
    SCHEDULE_TO_DC_NO_FORWARD,

    /**
     * Send the scheduling results of the {@link org.lgdcloudsim.interscheduler.InterScheduler} to the data center.
     * This type of scheduling not only clarifies which data center the instance group is scheduled to,
     * but also schedules the instances in the instance group at the host level.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.InstanceGroup} object.
     */
    SCHEDULE_TO_DC_HOST,

    /**
     * This event is used to respond to the {@link #SCHEDULE_TO_DC_HOST} event.
     * It indicates that all instances included in the event were successfully scheduled to the host without conflicts and failures.
     */
    SCHEDULE_TO_DC_HOST_OK,

    /**
     * This event is used to respond to the {@link #SCHEDULE_TO_DC_HOST} event.
     * It indicates that there are conflicts or failures in scheduling the instances to the host.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a {@link FailedOutdatedResult<InstanceGroup>} object.
     */
    SCHEDULE_TO_DC_HOST_CONFLICTED,

    /**
     * Denotes the start of the load balancing process.
     * It is used by the {@link org.lgdcloudsim.datacenter.Datacenter}.
     * It will call the {@link LoadBalancer} to distribute the instances to the intra-schedulers.
     */
    LOAD_BALANCE_SEND,

    /**
     * Denotes the end of the {@link org.lgdcloudsim.intrascheduler.IntraScheduler} scheduling.
     * The event will be sent after the execution of the {@link #INTRA_SCHEDULE_BEGIN}.
     */
    INTRA_SCHEDULE_END,

    /**
     * Conflict checking before placing any instance on the host.
     */
    PRE_ALLOCATE_RESOURCE,

    /**
     * Denotes the start of the {@link org.lgdcloudsim.intrascheduler.IntraScheduler} scheduling.
     */
    INTRA_SCHEDULE_BEGIN;

    /**
     * If there are multiple identical events at the same time, they only need to be executed once.
     */
    public static final Set<CloudActionTags> UNIQUE_TAG = Set.of(LOAD_BALANCE_SEND, PRE_ALLOCATE_RESOURCE, SYN_STATE_BY_HEARTBEAT_IN_DC);

    /**
     * Tags that exist in cycles.
     * If only these tags are left in all events,
     * it means that there are no new events that need to be executed, and the simulation can be ended.
     */
    public static final Set<CloudActionTags> LOOP_TAG = Set.of(SYN_STATE_BETWEEN_DC, SYN_STATE_BETWEEN_CENTER_AND_INTRA_SCHEDULER_IN_DC, CHANGE_COLLABORATION_SYN);

    /**
     * Convert the tag to a string.
     *
     * @param tag the tag to convert
     * @return the string representation of the tag
     */
    public static String tagToString(CloudActionTags tag) {
        return switch (tag) {
            case CHANGE_COLLABORATION_SYN -> "CHANGE_COLLABORATION_SYN";
            case SYN_STATE_BY_HEARTBEAT_IN_DC -> "SYN_STATE_BY_HEARTBEAT_IN_DC";
            case SYN_STATE_BETWEEN_CENTER_AND_INTRA_SCHEDULER_IN_DC ->
                    "SYN_STATE_BETWEEN_CENTER_AND_INTRA_SCHEDULER_IN_DC";
            case SYN_STATE_BETWEEN_DC -> "SYN_STATE_BETWEEN_DC";
            case SCHEDULE_TO_DC_NO_FORWARD -> "SCHEDULE_TO_DC_NO_FORWARD";
            case SCHEDULE_TO_DC_HOST -> "SCHEDULE_TO_DC_HOST";
            case SCHEDULE_TO_DC_HOST_OK -> "SCHEDULE_TO_DC_HOST_OK";
            case SCHEDULE_TO_DC_HOST_CONFLICTED -> "SCHEDULE_TO_DC_HOST_CONFLICTED";
            case USER_REQUEST_FAIL -> "USER_REQUEST_FAIL";
            case DC_REGISTRATION_REQUEST -> "DC_REGISTRATION_REQUEST";
            case DC_LIST_REQUEST -> "DC_LIST_REQUEST";
            case USER_REQUEST_SEND -> "USER_REQUEST_SEND";
            case NEED_SEND_USER_REQUEST -> "NEED_SEND_USER_REQUEST";
            case INTER_SCHEDULE_BEGIN -> "GROUP_FILTER_DC_BEGIN";
            case INTER_SCHEDULE_END -> "GROUP_FILTER_DC_END";
            case LOAD_BALANCE_SEND -> "LOAD_BALANCE_SEND";
            case INTRA_SCHEDULE_BEGIN -> "INTRA_SCHEDULE_BEGIN";
            case INTRA_SCHEDULE_END -> "INTRA_SCHEDULE_END";
            case PRE_ALLOCATE_RESOURCE -> "PRE_ALLOCATE_RESOURCE";
            case END_INSTANCE_RUN -> "END_INSTANCE_RUN";
            case NONE -> "NONE";
            default -> "UNKNOWN";
        };
    }
}

