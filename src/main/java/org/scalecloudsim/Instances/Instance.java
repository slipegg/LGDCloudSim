package org.scalecloudsim.Instances;

import org.cloudsimplus.core.UserEntity;
import org.cloudsimplus.hosts.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Instance extends UserEntity {
    Logger LOGGER = LoggerFactory.getLogger(Instance.class.getSimpleName());

    Instance NULL=new InstanceNull();

    void setId(long id);

    long getId();

//    String getDescription();
//
//    Instance setDescription(String description);

    double getLifeTime();

    Instance setLifeTime();
    InstanceGroup getInstanceGroup();

    Instance setInstanceGroup();

    Host getHost();

    //资源
    long getBw();

    long getRam();

    long getStorage();

    long getCpu();

    Instance setBw();

    Instance setRam();

    Instance setStorage();

    Instance setCpu();

    //状态：未到达(userEntity)，等待域间完成调度，域内等待，域内分配中，创建成功，创建失败,工作中，结束
//    boolean isWaitInterSchedule();
//
//    Instance setWaitInterSchedule();
//
//    boolean isInnerWaiting();
//
//    Instance setInnerWaiting();
//    boolean isInnerScheduling();
//
//    Instance setInnerScheduling();
//
//    boolean isCreated();
//    Instance setCreated();
//
//    boolean isFailed();
//    void setFailed(boolean failed);
//
//    boolean isWorking();
//
//    Instance setWorking();
//
//    boolean isFinish();
//
//    Instance setFinish();
//
//    double getStartTime();
//    Instance setStartTime(double startTime);
//    double getFinishTime();
//    Instance setFinishTime(double startTime);
}
