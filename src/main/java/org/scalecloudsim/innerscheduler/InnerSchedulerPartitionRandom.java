package org.scalecloudsim.innerscheduler;

import org.scalecloudsim.request.Instance;
import org.scalecloudsim.statemanager.SynState;

import java.util.*;

public class InnerSchedulerPartitionRandom extends InnerSchedulerSimple {
    Random random = new Random(1);

    public InnerSchedulerPartitionRandom(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    public Map<Integer, List<Instance>> scheduleInstances(List<Instance> instances, SynState synState) {
        int hostNum = datacenter.getStatesManager().getHostNum();
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
                int startHostId = random.nextInt(range[1] - range[0] + 1);
                int rangeLength = range[1] - range[0] + 1;
                for (int i = 0; i < rangeLength; i++) {
                    int hostId = range[0] + (startHostId + i) % rangeLength;
                    if (synState.isSuitable(hostId, instance)) {
                        suitId = hostId;
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
}
