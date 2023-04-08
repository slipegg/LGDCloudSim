package org.scalecloudsim.Instances;

import java.util.List;

public interface InstanceGroup extends RequestEntity {
    List<Instance> getInstanceList();

    InstanceGroup setInstanceList(List<Instance> instanceList);

    int getGroupType();

    InstanceGroup setGroupType(int type);

    int getDestDatacenterId();

    InstanceGroup setDestDatacenterId(int destDatacenterId);

    double getAcessLatency();

    InstanceGroup setAcessLatency(double latency);

    long getStorageSum();

    long getBwSum();

    long getCpuSum();

    long getRamSum();

    int getRetryNum();

    int getRetryMaxNum();

    int getState();

    InstanceGroup setState(int state);

    InstanceGroup setRetryNum(int retryNum);

    InstanceGroup setRetryMaxNum(int retryMaxNum);
}
