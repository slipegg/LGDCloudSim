package org.cpnsim.innerscheduler;

import lombok.Getter;
import lombok.Setter;
import org.cpnsim.request.Instance;
import org.cpnsim.datacenter.Datacenter;
import org.cpnsim.datacenter.InstanceQueue;
import org.cpnsim.datacenter.InstanceQueueFifo;
import org.cpnsim.statemanager.SynState;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
    @Getter
    InstanceQueue instanceQueue;
    @Getter
    @Setter
    InstanceQueue retryInstanceQueue;

    @Getter
    @Setter
    double scheduleCostTime = 0;

    @Getter
    @Setter
    int firstPartitionId = -1;

    int partitionNum = 0;

    @Getter
    double lastScheduleTime = 0;

    double excludeTime = 0;

    public InnerSchedulerSimple(Map<Integer, Double> partitionDelay) {
        this.partitionDelay = partitionDelay;
        //对于partitionDelay这个map，按照value从小到大排序，得到partitionTraverseList
        this.partitionTraverseList = partitionDelay.entrySet().
                stream().sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).collect(Collectors.toList());
        instanceQueue = new InstanceQueueFifo();
        retryInstanceQueue = new InstanceQueueFifo();
    }

    public InnerSchedulerSimple(int id, Map<Integer, Double> partitionDelay) {
        this(partitionDelay);
        setId(id);
    }

    public InnerSchedulerSimple(int id, int firstPartitionId, int partitionNum) {
        instanceQueue = new InstanceQueueFifo();
        retryInstanceQueue = new InstanceQueueFifo();
        this.firstPartitionId = firstPartitionId;
        this.partitionNum = partitionNum;
        setId(id);
    }

    public void setId(int id) {
        this.id = id;
        this.name = "InScheduler" + id;
    }

    @Override
    public InnerScheduler addInstance(List<Instance> instances, boolean isRetry) {
        if (isRetry) {
            retryInstanceQueue.add(instances);
        } else {
            instanceQueue.add(instances);
        }
        return this;
    }

    @Override
    public InnerScheduler addInstance(Instance instance, boolean isRetry) {
        if (isRetry) {
            retryInstanceQueue.add(instance);
        } else {
            instanceQueue.add(instance);
        }
        return this;
    }

    @Override
    public boolean isQueuesEmpty() {
        return instanceQueue.size() == 0 && retryInstanceQueue.size() == 0;
    }

    @Override
    public int getNewInstanceQueueSize() {
        return instanceQueue.size();
    }

    @Override
    public int getRetryInstanceQueueSize() {
        return retryInstanceQueue.size();
    }

    @Override
    public InnerSchedulerResult schedule() {
        SynState synState = datacenter.getStatesManager().getSynState(this);

        List<Instance> instances = getWaitSchedulingInstances();

        double startTime = System.currentTimeMillis();
        InnerSchedulerResult innerSchedulerResult = scheduleInstances(instances, synState);
        double endTime = System.currentTimeMillis();

        lastScheduleTime = datacenter.getSimulation().clock();

        this.scheduleCostTime = Math.max(0, (endTime - startTime) - excludeTime);//= BigDecimal.valueOf((instances.size() * 0.25)).setScale(datacenter.getSimulation().getSimulationAccuracy(), RoundingMode.HALF_UP).doubleValue();//* instances.size();//(endTime-startTime)/10;

        return innerSchedulerResult;
    }

    private List<Instance> getWaitSchedulingInstances() {
        List<Instance> instances = new ArrayList<>();
        if (retryInstanceQueue.size() != 0) {
            instances = retryInstanceQueue.getBatchItem(true);
        } else {
            instances = instanceQueue.getBatchItem(true);
        }
        return instances;
    }

    protected InnerSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        InnerSchedulerResult innerSchedulerResult = new InnerSchedulerResult(this, getDatacenter().getSimulation().clock());

        for (Instance instance : instances) {
            int suitId = -1;

            int synPartitionId = firstPartitionId;
            if (datacenter.getStatesManager().isSynCostTime()) {
                synPartitionId = (firstPartitionId + datacenter.getStatesManager().getSmallSynGapCount()) % partitionNum;
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
                instance.setExpectedScheduleHostId(suitId);
                innerSchedulerResult.addScheduledInstance(instance);
            } else {
                innerSchedulerResult.addFailedScheduledInstance(instance);
            }
        }
        return innerSchedulerResult;
    }

    @Override
    public void setDatacenter(Datacenter datacenter) {
        this.datacenter = datacenter;
    }
}
