package org.cpnsim.request;

import org.cpnsim.core.ChangeableId;

import java.util.List;

public interface UserRequest extends ChangeableId {
    static int WAITING = -1;
    static int FAILED = 0;
    static int SCHEDULING = 1;
    static int SUCCESS = 2;
    static int RUNNING = 3;

    public static String stateToString(int state) {
        return switch (state) {
            case WAITING -> "WAITING";
            case FAILED -> "FAILED";
            case SCHEDULING -> "SCHEDULING";
            case SUCCESS -> "SUCCESS";
            case RUNNING -> "RUNNING";
            default -> "UNKNOWN";
        };
    }

    List<InstanceGroup> getInstanceGroups();

    UserRequest setInstanceGroups(List<InstanceGroup> instanceGroups);

    InstanceGroupGraph getInstanceGroupGraph();

    UserRequest setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph);

    UserRequest setSubmitTime(double submitTime);

    double getSubmitTime();

    UserRequest setFinishTime(double finishTime);

    double getFinishTime();

    //TODO 后期可以考虑用地理位置来查找属于的dc
    int getBelongDatacenterId();

    UserRequest setBelongDatacenterId(int belongDatacenterId);

    int getState();

    UserRequest setState(int state);

    UserRequest addSuccessGroupNum();

    UserRequest addFailReason(String failReason);

    String getFailReason();

    UserRequest addAllocatedEdge(InstanceGroupEdge edge);

    UserRequest delAllocatedEdge(InstanceGroupEdge edge);

    List<InstanceGroupEdge> getAllocatedEdges();

    String getArea();

    UserRequest setArea(String area);
}
