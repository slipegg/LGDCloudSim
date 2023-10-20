package org.cpnsim.innerscheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.SynState;

import java.util.*;

public class InnerSchedulerFixedPartitionRandom extends InnerSchedulerSimple {
    Random random = new Random();

    public InnerSchedulerFixedPartitionRandom(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    public Map<Integer, List<Instance>> scheduleInstances(List<Instance> instances, SynState synState) {
        Map<Integer, List<Instance>> res = new HashMap<>();
        int partitionNum = datacenter.getInnerSchedulers().size();
        int hostSum = datacenter.getStatesManager().getHostNum();
        int firstPartitionId = this.id;
        for (Instance instance : instances) {
            int suitId = -1;
            for (int p = 0; p < partitionNum; p++) {
                int partId = (firstPartitionId + partitionNum - p) % partitionNum;
                int startId = partId * (hostSum / partitionNum);
                int rangeLength;
                if (partId == partitionNum - 1) {
                    rangeLength = hostSum - startId;
                } else {
                    rangeLength = (partId + 1) * (hostSum / partitionNum) - startId;
                }
                int selectId = random.nextInt(rangeLength);
                for (int i = 0; i < rangeLength; i++) {
                    int hostId = startId + (selectId + i) % rangeLength;
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
