package org.scalecloudsim.request;

import org.cloudsimplus.core.ChangeableId;

import java.util.List;

public interface UserRequest extends ChangeableId {
    static int WAITING = -1;
    static int FAILED = 0;
    static int SUCCESS = 1;

    List<InstanceGroup> getInstanceGroups();

    UserRequest setInstanceGroups(List<InstanceGroup> instanceGroups);

    InstanceGroupGraph getInstanceGroupGraph();

    UserRequest setInstanceGroupGraph(InstanceGroupGraph instanceGroupGraph);

    UserRequest setSubmitTime(double submitTime);

    double getSubmitTime();

    //TODO 后期可以考虑用地理位置来查找属于的dc
    int getBelongDatacenterId();

    UserRequest setBelongDatacenterId(int belongDatacenterId);

    int getState();

    UserRequest setState(int state);
}
