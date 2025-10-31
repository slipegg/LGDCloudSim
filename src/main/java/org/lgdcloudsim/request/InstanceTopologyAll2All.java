package org.lgdcloudsim.request;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Generated;

import lombok.Getter;
import lombok.Setter;

public class InstanceTopologyAll2All implements InstanceTopology {
    private List<Instance> instanceList;

    @Getter
    @Setter
    private TrainingStrategy trainingStrategy;

    public InstanceTopologyAll2All(List<Instance> instanceList, TrainingStrategy trainingStrategy) {
        this.instanceList = instanceList;
        this.trainingStrategy = trainingStrategy;
    }

    @Override
    public Instance getMaxScoreInstance() {
        Instance maxInstance = null;
        for (Instance instance : instanceList) {
            if (instance.getExpectedScheduleHostId() < 0) {
                maxInstance = instance;
                break;
            }
        }
        return maxInstance;
    }

    @Override
    public List<Instance> getLinkedInstances(Instance instance) {
        List<Instance> linkedInstances = new ArrayList<>();
        for (Instance inst : instanceList) {
            if (!inst.equals(instance) && inst.getExpectedScheduleHostId() < 0) {
                linkedInstances.add(inst);
            }
        }
        return linkedInstances;
    }

    @Override
    public List<Instance> getAllInstances() {
        return instanceList;
    }

    @Override
    public String getType() {
        return All2All;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InstanceTopologyAll2All{");
        sb.append("instanceList=[");
        for (Instance instance : instanceList) {
            sb.append(instance.getId()).append(",");
        }
        if (!instanceList.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]}");
        return sb.toString();
    }
}
