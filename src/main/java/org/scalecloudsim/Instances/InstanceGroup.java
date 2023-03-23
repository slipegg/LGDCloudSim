package org.scalecloudsim.Instances;

import org.cloudsimplus.core.CloudSimTag;
import org.cloudsimplus.core.UserEntity;

import java.util.List;

public interface InstanceGroup  extends UserEntity {
    InstanceGroup NULL=new InstanceGroupNull();
//    String getDescription();
//
//    InstanceGroup setDescription(String description);

    List<Instance> getInstanceList();

    InstanceGroup setInstanceList(List<Instance> instanceList);

    int getGroupType();
    InstanceGroup setGroupType(int tag);

    //状态： 未提交（userEntity）,域间调度等待，域间调度中，域间调度成功，域间调度失败
//    boolean isInterWaiting();
//    InstanceGroup setInterWaiting();
//
//    boolean isInterScheduling();
//    InstanceGroup setInterScheduling();
//    double getInterSchedulingStartTime();
//
//    boolean isInterScheduledSuccess();
//    InstanceGroup setInterScheduledSuccess();
//    double getInterScheduledTime();
//
//    boolean isInterScheduledFail();
//    InstanceGroup setInterScheduledFail();
}
