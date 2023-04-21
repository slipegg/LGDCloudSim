package org.scalecloudsim.request;

import org.scalecloudsim.datacenter.Datacenter;

import java.util.List;

public interface InstanceGroup extends RequestEntity {
    List<Instance> getInstanceList();

    InstanceGroup setInstanceList(List<Instance> instanceList);

    int getGroupType();

    InstanceGroup setGroupType(int type);

    int getDestDatacenterId();

    InstanceGroup setDestDatacenterId(int destDatacenterId);

    double getAccessLatency();

    InstanceGroup setAccessLatency(double latency);

    long getStorageSum();

    long getBwSum();

    long getCpuSum();

    long getRamSum();

    InstanceGroup addRetryNum();

    boolean isFailed();

    int getRetryNum();

    int getRetryMaxNum();

    int getState();

    InstanceGroup setState(int state);

    InstanceGroup setRetryNum(int retryNum);

    InstanceGroup setRetryMaxNum(int retryMaxNum);

    InstanceGroup setReceiveDatacenter(Datacenter receiveDatacenter);

    Datacenter getReceiveDatacenter();
}
