package org.cpnsim.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    int getLifeTime();

    Instance setLifeTime(int lifeTime);

    InstanceGroup getInstanceGroup();

    Instance setInstanceGroup(InstanceGroup instanceGroup);

    Instance setDestHost(int destHost);

    int getDestHost();

    boolean isSetDestHost();

    //记录成功相关的信息
    int getHost();

    Instance setHost(int host);

    double getStartTime();

    Instance setStartTime(double startTime);

    double getFinishTime();

    Instance setFinishTime(double finishTime);

    Instance addRetryNum();

    boolean isFailed();

    int getRetryNum();

    int getRetryMaxNum();

    int getState();

    Instance setState(int state);

    Instance setRetryNum(int retryNum);

    Instance setRetryMaxNum(int retryMaxNum);
}
