package org.cpnsim.innerscheduler;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.Instance;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.InstanceQueue;
import org.cpnsim.datacenter.InstanceQueueFifo;
import org.cpnsim.statemanager.SynState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InnerSchedulerSimple implements InnerScheduler {
    @Getter
    Datacenter datacenter;
    @Getter
    @Setter
    Map<Integer, Double> partitionDelay;
    List<Integer> partitionTraverseList;
    @Getter
    int id;
    @Getter
    @Setter
    String name;
    InstanceQueue instanceQueue;

    @Getter
    @Setter
    double scheduleCostTime = 0;

    @Getter
    @Setter
    int firstPartitionId = -1;

    int partitionNum = 0;

    @Getter
    double lastScheduleTime = 0;

    public InnerSchedulerSimple(Map<Integer, Double> partitionDelay) {
        this.partitionDelay = partitionDelay;
        //对于partitionDelay这个map，按照value从小到大排序，得到partitionTraverseList
        this.partitionTraverseList = partitionDelay.entrySet().
                stream().sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).collect(Collectors.toList());
        instanceQueue = new InstanceQueueFifo(100);
    }

    public InnerSchedulerSimple(int id, Map<Integer, Double> partitionDelay) {
        this(partitionDelay);
        setId(id);
    }

    public InnerSchedulerSimple(int id, int firstPartitionId, int partitionNum) {
        instanceQueue = new InstanceQueueFifo(100000);
        this.firstPartitionId = firstPartitionId;
        this.partitionNum = partitionNum;
        setId(id);
    }

    public void setId(int id) {
        this.id = id;
        this.name = "InScheduler" + id;
    }

    @Override
    public InnerScheduler addInstance(List<Instance> instances) {
        instanceQueue.add(instances);
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
    public int getQueueSize() {
        return instanceQueue.size();
    }

    @Override
    public Map<Integer, List<Instance>> schedule() {
        //TODO 域内调度
        List<Instance> instances = instanceQueue.getBatchItem();
        SynState synState = datacenter.getStatesManager().getSynState(this);
        double startTime = System.currentTimeMillis();
        Map<Integer, List<Instance>> res = scheduleInstances(instances, synState);
        double endTime = System.currentTimeMillis();
        lastScheduleTime = datacenter.getSimulation().clock();
        this.scheduleCostTime = 0.25;//* instances.size();//(endTime-startTime)/10;
        LOGGER.info("{}: {}'s {} starts scheduling {} instances,cost {} ms", datacenter.getSimulation().clockStr(), datacenter.getName(), getName(), instances.size(), scheduleCostTime);
        return res;
    }

    public Map<Integer, List<Instance>> scheduleInstances(List<Instance> instances, SynState synState) {
        //TODO 域内调度
        Map<Integer, List<Instance>> res = new HashMap<>();
        for (Instance instance : instances) {
            int suitId = -1;

            int synPartitionId = firstPartitionId;
            if (datacenter.getStatesManager().getSmallSynGap() != 0) {
                int smallSynNum = (int) (datacenter.getSimulation().clock() / datacenter.getStatesManager().getSmallSynGap());
                synPartitionId = (firstPartitionId + smallSynNum) % partitionNum;
            }
            for (int p = 0; p < partitionNum; p++) {
                int[] range = datacenter.getStatesManager().getPartitionRangesManager().getRange((synPartitionId + p) % partitionNum);
                for (int i = range[0]; i <= range[1]; i++) {
                    if (synState.isSuitable(i, instance)) {
                        suitId = i;
                        break;
                    }
                }
                if (suitId != -1) {
                    break;
                }
            }
            if (suitId != -1) {
                synState.allocateTmpResource(suitId, instance);
            }
            res.putIfAbsent(suitId, new ArrayList<>());
            res.get(suitId).add(instance);
        }
        return res;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
    }
}
