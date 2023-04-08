package org.scalecloudsim.innerscheduler;

import lombok.Getter;
import lombok.Setter;
import org.scalecloudsim.Instances.Instance;
import org.scalecloudsim.datacenters.Datacenter;
import org.scalecloudsim.datacenters.InstanceQueue;
import org.scalecloudsim.datacenters.InstanceQueueFifo;

import java.util.List;
import java.util.Map;

public class InnerSchedulerSimple implements InnerScheduler {
    @Getter
    Datacenter datacenter;
    @Getter
    @Setter
    Map<Integer, Double> partitionDelay;
    @Getter
    int id;
    @Getter
    @Setter
    String name;
    InstanceQueue instanceQueue;

    public InnerSchedulerSimple(Map<Integer, Double> partitionDelay) {
        this.partitionDelay = partitionDelay;
        instanceQueue = new InstanceQueueFifo();
    }

    public InnerSchedulerSimple(int id, Map<Integer, Double> partitionDelay) {
        this(partitionDelay);
        this.id = id;
    }

    @Override
    public InnerScheduler addInstance(List<Instance> instances) {
        for (Instance instance : instances) {
            instanceQueue.add(instance);
        }
        return this;
    }

    @Override
    public InnerScheduler addInstance(Instance instance) {
        instanceQueue.add(instance);
        return this;
    }

    @Override
    public boolean isQueueEmpty() {
        return instanceQueue.size() == 0;
    }

    @Override
    public InnerScheduler schedule() {
        //TODO 域内调度
        return this;
    }
}
