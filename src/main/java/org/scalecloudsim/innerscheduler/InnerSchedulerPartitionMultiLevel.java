package org.scalecloudsim.innerscheduler;

import org.scalecloudsim.request.Instance;
import org.scalecloudsim.statemanager.PartitionRangesManager;
import org.scalecloudsim.statemanager.SynState;

import java.util.*;

public class InnerSchedulerPartitionMultiLevel extends InnerSchedulerSimple {
    Random random = new Random();

    public InnerSchedulerPartitionMultiLevel(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    public Map<Integer, List<Instance>> scheduleInstances(List<Instance> instances, SynState synState) {
        Map<Integer, List<Instance>> res = new HashMap<>();
        for (Instance instance : instances) {
            int suitId = getSuitHostId(synState, instance);
            res.putIfAbsent(suitId, new ArrayList<>());
            res.get(suitId).add(instance);
        }
        return res;
    }

    int getSuitHostId(SynState synState, Instance instance) {
        int synPartitionId = firstPartitionId;
        if (datacenter.getStatesManager().getSmallSynGap() != 0) {
            int smallSynNum = (int) (datacenter.getSimulation().clock() / datacenter.getStatesManager().getSmallSynGap());
            synPartitionId = (firstPartitionId + smallSynNum) % partitionNum;
        }
        PartitionRangesManager partitionRangesManager = datacenter.getStatesManager().getPartitionRangesManager();
        int partitionNum = partitionRangesManager.getPartitionNum();

        //首次同步的分区调度
        int partitionLength = partitionRangesManager.getRangeLength(synPartitionId);
        int groupNum = partitionLength / partitionNum;
        int startGroupId = random.nextInt(groupNum);
        for (int _groupPartitionId = 0; _groupPartitionId < partitionNum; _groupPartitionId++) {//遍历组内的host
            int groupPartitionId = (firstPartitionId + _groupPartitionId) % partitionNum;
            for (int _groupId = 0; _groupId < groupNum; _groupId++) {//遍历分组
                int groupId = (startGroupId + _groupId) % groupNum;
                int hostId = partitionRangesManager.getRange(synPartitionId)[0] + groupId * partitionNum + groupPartitionId;
                if (synState.isSuitable(hostId, instance)) {
                    synState.allocateTmpResource(hostId, instance);
                    return hostId;
                }
            }
        }

        //其他分区调度
        for (int _partitionId = 1; _partitionId < partitionNum; _partitionId++) {//遍历分区
            int partitionId = (synPartitionId + _partitionId) % partitionNum;
            for (int _groupPartitionId = 0; _groupPartitionId < partitionNum; _groupPartitionId++) {//遍历组内的host
                int groupPartitionId = (firstPartitionId + _groupPartitionId) % partitionNum;
                groupNum = partitionRangesManager.getRangeLength(partitionId) / partitionNum;
                startGroupId = random.nextInt(groupNum);
                for (int _groupId = 0; _groupId < groupNum; _groupId++) {//遍历分组
                    int groupId = (startGroupId + _groupId) % groupNum;
                    int hostId = partitionRangesManager.getRange(partitionId)[0] + groupId * partitionNum + groupPartitionId;
                    if (synState.isSuitable(hostId, instance)) {
                        synState.allocateTmpResource(hostId, instance);
                        return hostId;
                    }
                }
            }
        }
        return -1;
    }
}
