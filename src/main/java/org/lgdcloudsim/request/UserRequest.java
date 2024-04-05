package org.lgdcloudsim.request;

import org.lgdcloudsim.core.ChangeableId;

import java.util.List;

/**
 * UserRequest is the interface for user requests.
 * Each user request contains multiple {@link InstanceGroup},
 * and each {@link InstanceGroup} contains multiple {@link Instance}.
 * Instances in the same instance group must be scheduled to the same data center.
 * Failure to schedule any instance indicates that the user request for scheduling failed.
 * In the case of affinity requests, each instance group may protect access latency constraints,
 * and there may also be latency and bandwidth constraints between each instance group,
 * represented by an instance group graph.
 * Each user request may also contain access delay constraints.
 *
 * @author Anonymous
 * @since LGDCloudSim 1.0
 */

public interface UserRequest extends ChangeableId {
    /**
     * The state of the user request entity which including UserRequest, InstanceGroup and Instance.
     * The user request entity is waiting to be scheduled.
     * All user requests entity are in this state when they are first created.
     */
    int WAITING = -1;

    /**
     * The user request entity failed to be scheduled.
     */
    int FAILED = 0;

    /**
     * The user request entity is being scheduled.
     * This state occurs when the instance group is received by the data center
     * and is waiting for its instances to be scheduled to hosts.
     */
    int SCHEDULING = 1;

    /**
     * The user request has been successfully scheduled.
     */
    int SUCCESS = 2;

    /**
     * The user request is running. This state occurs when the instance is running on the host.
     */
    int RUNNING = 3;

    /**
     * Convert the state of the user request entity to a string.
     *
     * @param state the state of the user request entity.
     * @return the string representation of the state of the user request entity.
     */
    static String stateToString(int state) {
        return switch (state) {
            case WAITING -> "WAITING";
            case FAILED -> "FAILED";
            case SCHEDULING -> "SCHEDULING";
            case SUCCESS -> "SUCCESS";
            case RUNNING -> "RUNNING";
            default -> "UNKNOWN";
        };
    }

    /**
     * Get all instance groups in the user request.
     * @return all instance groups in the user request.
     */
    List<InstanceGroup> getInstanceGroups();

    /**
     * Set all instance groups in the user request.
     * @param instanceGroups all instance groups in the user request.
     * @return the user request itself.
     */
    UserRequest setInstanceGroups(List<InstanceGroup> instanceGroups);

    /**
     * Get the instance group graph of the user request.
     * @return the instance group graph of the user request.
     */
    InstanceGroupGraph getInstanceGroupGraph();

    /**
     * Set the instance group graph of the user request.
     * @param instanceGroupGraph the instance group graph of the user request.
     * @return the user request itself.
     */
    UserRequest setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph);

    /**
     * Get the submit time of the user request.
     * Note: LGDCloudSim ignores the time period sent by the user to the data center or cloud administrator.
     * The user request submission time is the time it is received by the data center or cloud administrator.
     * @return the submit time of the user request.
     */
    double getSubmitTime();

    /**
     * Set the submit time of the user request.
     *
     * @param submitTime the submit time of the user request.
     * @return the user request itself.
     */
    UserRequest setSubmitTime(double submitTime);

    /**
     * Get the finish time of the user request.
     * @return the finish time of the user request.
     */
    double getFinishTime();

    /**
     * Set the finish time of the user request.
     *
     * @param finishTime the finish time of the user request.
     * @return the user request itself.
     */
    UserRequest setFinishTime(double finishTime);

    /**
     * Get the data center ID to which the user request belongs to.
     * The user request will be send to the data center with this ID.
     * This data center can be considered as the data center closest to the user request
     * TODO: Later, we can consider using geographical location area to find the data center it belongs to.
     * @return the data center ID to which the user request is scheduled.
     */
    int getBelongDatacenterId();

    /**
     * Set the data center ID to which the user request belongs to.
     * @param belongDatacenterId the data center ID to which the user request is scheduled.
     * @return the user request itself.
     */
    UserRequest setBelongDatacenterId(int belongDatacenterId);

    /**
     * Get the state of the user request entity.
     * @return the state of the user request entity.
     */
    int getState();

    /**
     * Set the state of the user request entity.
     * @param state the state of the user request entity.
     * @return the user request itself.
     */
    UserRequest setState(int state);

    /**
     * Add the number of instance groups that have been successfully scheduled.
     * If all the instance groups in the user request have been successfully scheduled, the user request is marked as success.
     * @return the user request itself.
     */
    UserRequest addSuccessGroupNum();

    /**
     * Add the fail reason of the user request entity.
     * @param failReason the fail reason of the user request entity.
     * @return the user request itself.
     */
    UserRequest addFailReason(String failReason);

    /**
     * Get the fail reason of the user request.
     * @return the fail reason of the user request.
     */
    String getFailReason();

    /**
     * Add an edge that has rented bandwidth resources{@link InstanceGroupGraph#getBw(InstanceGroup, InstanceGroup)}.
     * @param edge the edge that has rented bandwidth resources.
     * @return the user request itself.
     */
    UserRequest addAllocatedEdge(InstanceGroupEdge edge);

    /**
     * Remove an edge that has rented bandwidth resources{@link InstanceGroupGraph#getBw(InstanceGroup, InstanceGroup)}.
     * @param edge the edge that has rented bandwidth resources.
     * @return the user request itself.
     */
    UserRequest delAllocatedEdge(InstanceGroupEdge edge);

    /**
     * Get the list of edges that have rented bandwidth resources{@link InstanceGroupGraph#getBw(InstanceGroup, InstanceGroup)}.
     * @return the list of edges that have rented bandwidth resources.
     */
    List<InstanceGroupEdge> getAllocatedEdges();

    /**
     * Get the area of the user to which the user request belongs.
     * The network module will calculate the access delay between the user and the data center
     * based on the region where the user belongs and the region where the data center belongs.
     * @see org.lgdcloudsim.network.AreaDelayManager
     * @return the area of the user to which the user request belongs.
     */
    String getArea();

    /**
     * Set the area of the user to which the user request belongs.
     * @param area the area of the user to which the user request belongs.
     * @return the user request itself.
     */
    UserRequest setArea(String area);

    /**
     * Get the schedule delay limit of the user request.
     * Every instance of the user request must be scheduled within the schedule delay limit.
     * The schedule delay = the time the instance starts running on the host - the time the user request to which the instance belongs is submitted.
     * If the user request is not scheduled within the schedule delay limit, the user request is marked as failed.
     * If the schedule delay limit is less than 0, the user request is not limited by the schedule delay.
     * @return the schedule delay limit of the user request.
     */
    double getScheduleDelayLimit();

    /**
     * Set the schedule delay limit of the user request.
     * Every instance of the user request must be scheduled within the schedule delay limit.
     * The schedule delay = the time the instance starts running on the host - the time the user request to which the instance belongs is submitted.
     * If the user request is not scheduled within the schedule delay limit, the user request is marked as failed.
     * If the schedule delay limit is less than 0, the user request is not limited by the schedule delay.
     * @param scheduleDelayLimit the schedule delay limit of the user request.
     * @return the user request itself.
     */
    UserRequest setScheduleDelayLimit(double scheduleDelayLimit);
}
