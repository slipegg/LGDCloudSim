package org.scalecloudsim.Instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface Instance extends RequestEntity {
    Logger LOGGER = LoggerFactory.getLogger(Instance.class.getSimpleName());

    //资源
    int getCpu();

    int getRam();

    int getStorage();

    int getBw();

    Instance setCpu(int cpu);

    Instance setRam(int ram);

    Instance setStorage(int storage);

    Instance setBw(int bw);

    //自身的其他有关请求的属性
    double getLifeTime();

    Instance setLifeTime(double lifeTime);

    InstanceGroup getInstanceGroup();

    Instance setInstanceGroup(InstanceGroup instanceGroup);

    Instance setDestHost(int destHost);

    int getDestHost();

    boolean isSetDestHost();

    int getMaxFailNum();

    Instance setMaxFailNum(int maxFailNum);

    //调度结果
    //记录失败相关的信息
    int getFailNum();

    Instance setFailNum(int failNum);

    List<InstanceFailInfo> getFailedHosts();

    Instance addFailedInfo(InstanceFailInfo instanceFailInfo);

    //记录成功相关的信息
    int getHost();

    Instance setHost(int host);

    double getStartTime();

    Instance setStartTime(double startTime);

    double getFinishTime();

    Instance setFinishTime(double finishTime);

    //状态
    int getStatus();

    Instance setStatus(int status);
}
