package org.lgdcloudsim.core;

import org.lgdcloudsim.loadbalancer.LoadBalancer;
import org.lgdcloudsim.interscheduler.InterSchedulerResult;
import org.lgdcloudsim.request.InstanceGroup;
import org.lgdcloudsim.util.FailedOutdatedResult;

import java.util.Set;

/**
 * Tags indicating a type of action that
 * needs to be undertaken by CloudSim entities when they receive or send events.
 * The lower the value of Tag, the higher the execution priority.
 *
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @author Anthony Sulistio
 * @author Manoel Campos da Silva Filho
 * @author Anonymous
 * @since CloudSim Toolkit 1.0
 */
public class CloudSimTag {
    public static final int NONE = -99;

    /**
     * Starting constant value for cloud-related tags.
     */
    private static final int BASE = 0;

    /**
     * Denotes a request from a Datacenter to register itself. This tag is normally used
     * between {@link CloudInformationService} and Datacenter entities.
     * When such a {@link org.lgdcloudsim.core.events.SimEvent} is sent, the {@link org.lgdcloudsim.core.events.SimEvent#getData()}
     * must be a {@link org.lgdcloudsim.datacenter.Datacenter} object.
     */
    public static final int DC_REGISTRATION_REQUEST = BASE - 7;

    /**
     * Periodically modify the scope of each collaboration zone.
     * This tag is used to notify the {@link CloudInformationService} to change the collaboration zone.
     */
    public static final int CHANGE_COLLABORATION_SYN = BASE - 6;

    /**
     * Denotes the end of the execution of instances.
     * This tag is used by the {@link org.lgdcloudsim.datacenter.Datacenter}.
     * the {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.Instance} objects
     * which are finishing at the same time.
     */
    public static final int END_INSTANCE_RUN = -5;

    /**
     * Synchronize host states to intra-scheduler in the data center.
     * It will be executed periodically.
     */
    public static final int SYN_STATE_IN_DC = BASE - 4;

    /**
     * Synchronize host states to inter-scheduler between data centers.
     * It will be executed periodically.
     */
    public static final int SYN_STATE_BETWEEN_DC = BASE - 3;

    /**
     * Denotes that some user request has failed.
     * This event will be sent to {@link CloudInformationService} for processing.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a set of {@link org.lgdcloudsim.request.UserRequest} object.
     */
    public static final int USER_REQUEST_FAIL = BASE - 1;

    /**
     * Denotes a request from the {@link org.lgdcloudsim.user.UserSimple} to a {@link CloudInformationService} to get
     * the list of all Datacenters which are registered with the CloudInformationService.
     */
    public static final int DC_LIST_REQUEST = BASE + 2;

    /**
     * Denotes a request from the {@link org.lgdcloudsim.user.UserSimple} to send.
     * It can be sent to the {@link CloudInformationService} or to a {@link org.lgdcloudsim.datacenter.Datacenter}.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.UserRequest} object.
     * It also can be the {@link org.lgdcloudsim.interscheduler.InterScheduler} scheduling result.
     * In this case, the instanceGroups can be forwarded to other data centers.
     */
    public static final int USER_REQUEST_SEND = BASE + 3;

    /**
     * Denotes that there are still user requests that need to be sent by the {@link org.lgdcloudsim.user.UserSimple}.
     */
    public static final int NEED_SEND_USER_REQUEST = BASE + 4;

    /**
     * Denotes the start of inter-scheduler scheduling.
     * Note that it must be bigger than {@link #USER_REQUEST_SEND}.
     * Therefore, when two events arrive at the same time,
     * the user request can be placed in the queue first, and then inter-scheduler scheduling can be started.
     */
    public static final int INTER_SCHEDULE_BEGIN = BASE + 5;

    /**
     * Denotes the end of inter-scheduler scheduling.
     * The event will be sent after the execution of the {@link #INTER_SCHEDULE_BEGIN}.
     * The time interval between two events is the time spent on inter-scheduling.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link InterSchedulerResult} object.
     */
    public static final int INTER_SCHEDULE_END = INTER_SCHEDULE_BEGIN + 1;

    /**
     * Send the scheduling results of the {@link org.lgdcloudsim.interscheduler.InterScheduler} to the data center.
     * This type of scheduling only clarifies which data center the instance group is scheduled to
     * and does not perform host-level scheduling on the instances in the instance group.
     * After the instance group is sent to the data center,
     * the data center does not allow the instance group to be forwarded to other data centers.
     * So the datacenter will call intra-scheduler to schedule the instance group.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.InstanceGroup} object.
     */
    public static final int SCHEDULE_TO_DC_NO_FORWARD = INTER_SCHEDULE_END + 1;

    /**
     * Send the scheduling results of the {@link org.lgdcloudsim.interscheduler.InterScheduler} to the data center.
     * This type of scheduling not only clarifies which data center the instance group is scheduled to,
     * but also schedules the instances in the instance group at the host level.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a list of {@link org.lgdcloudsim.request.InstanceGroup} object.
     */
    public static final int SCHEDULE_TO_DC_HOST = SCHEDULE_TO_DC_NO_FORWARD + 1;

    /**
     * This event is used to respond to the {@link #SCHEDULE_TO_DC_HOST} event.
     * It indicates that all instances included in the event were successfully scheduled to the host without conflicts and failures.
     */
    public static final int SCHEDULE_TO_DC_HOST_OK = SCHEDULE_TO_DC_HOST + 1;

    /**
     * This event is used to respond to the {@link #SCHEDULE_TO_DC_HOST} event.
     * It indicates that there are conflicts or failures in scheduling the instances to the host.
     * The {@link org.lgdcloudsim.core.events.SimEvent#getData()} must be a {@link FailedOutdatedResult < InstanceGroup >} object.
     */
    public static final int SCHEDULE_TO_DC_HOST_CONFLICTED = SCHEDULE_TO_DC_HOST_OK + 1;

    /**
     * Denotes the start of the load balancing process.
     * It is used by the {@link org.lgdcloudsim.datacenter.Datacenter}.
     * It will call the {@link LoadBalancer} to distribute the instances to the intra-schedulers.
     */
    public static final int LOAD_BALANCE_SEND = SCHEDULE_TO_DC_HOST_CONFLICTED + 1;

    /**
     * Denotes the end of the {@link org.lgdcloudsim.intrascheduler.IntraScheduler} scheduling.
     * The event will be sent after the execution of the {@link #INTRA_SCHEDULE_BEGIN}.
     */
    public static final int INTRA_SCHEDULE_END = LOAD_BALANCE_SEND + 1;

    /**
     * Conflict checking before placing any instance on the host.
     */
    public static final int PRE_ALLOCATE_RESOURCE = INTRA_SCHEDULE_END + 1;

    /**
     * Denotes the start of the {@link org.lgdcloudsim.intrascheduler.IntraScheduler} scheduling.
     */
    public static final int INTRA_SCHEDULE_BEGIN = PRE_ALLOCATE_RESOURCE + 1;

    /**
     * If there are multiple identical events at the same time, they only need to be executed once.
     */
    public static final Set<Integer> UNIQUE_TAG = Set.of(LOAD_BALANCE_SEND, PRE_ALLOCATE_RESOURCE);

    /**
     * Tags that exist in cycles.
     * If only these tags are left in all events,
     * it means that there are no new events that need to be executed, and the simulation can be ended.
     */
    public static final Set<Integer> LOOP_TAG = Set.of(SYN_STATE_BETWEEN_DC, SYN_STATE_IN_DC, CHANGE_COLLABORATION_SYN);

    /**
     * Convert the tag to a string.
     *
     * @param tag the tag to convert
     * @return the string representation of the tag
     */
    public static String tagToString(int tag) {
        return switch (tag) {
            case CHANGE_COLLABORATION_SYN -> "CHANGE_COLLABORATION_SYN";
            case SYN_STATE_IN_DC -> "SYN_STATE_IN_DC";
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
            case BASE -> "Base";
            default -> "UNKNOWN";
        };
    }
}
