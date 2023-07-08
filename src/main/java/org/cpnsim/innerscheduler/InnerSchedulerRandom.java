package org.cpnsim.innerscheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.SynState;

import java.util.*;

public class InnerSchedulerRandom extends InnerSchedulerSimple {
    Random random = new Random();

    public InnerSchedulerRandom(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    public Map<Integer, List<Instance>> scheduleInstances(List<Instance> instances, SynState synState) {
        int hostNum = datacenter.getStatesManager().getHostNum();
        //TODO 域内调度
        Map<Integer, List<Instance>> res = new HashMap<>();

        for (Instance instance : instances) {
            int suitId = -1;

            int startHostId = random.nextInt(hostNum);
            for (int i = 0; i < hostNum; i++) {
                int hostId = (startHostId + i) % hostNum;
                if (synState.isSuitable(hostId, instance)) {
                    suitId = hostId;
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
