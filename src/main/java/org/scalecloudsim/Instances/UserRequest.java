package org.scalecloudsim.Instances;

import org.cloudsimplus.core.ChangeableId;
import org.scalecloudsim.datacenters.Datacenter;

import java.util.List;

public interface UserRequest extends ChangeableId {
    public static int WAITING = -1;
    public static int FAILED = 0;
    public static int SUCCESS = 1;

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
