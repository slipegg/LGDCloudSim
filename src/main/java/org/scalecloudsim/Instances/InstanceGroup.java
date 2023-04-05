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
}
