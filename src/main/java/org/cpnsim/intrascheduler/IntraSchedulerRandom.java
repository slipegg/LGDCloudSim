package org.cpnsim.intrascheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.SynState;

import java.util.*;

public class IntraSchedulerRandom extends IntraSchedulerSimple {
    Random random = new Random();

    public IntraSchedulerRandom(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());

        List<Integer> innerSchedulerView = datacenter.getStatesManager().getIntraSchedulerView(this);
        int hostNum = innerSchedulerView.get(1)-innerSchedulerView.get(0)+1;

        for (Instance instance : instances) {
            int suitId = -1;

            int startHostId = random.nextInt(hostNum);
            for (int i = 0; i < hostNum; i++) {
                int hostId = (startHostId + i) % hostNum+innerSchedulerView.get(0);
                if (synState.isSuitable(hostId, instance)) {
                    suitId = hostId;
                    break;
                }
            }

            if (suitId != -1) {
                synState.allocateTmpResource(suitId, instance);
                instance.setExpectedScheduleHostId(suitId);
                intraSchedulerResult.addScheduledInstance(instance);
            } else {
                intraSchedulerResult.addFailedScheduledInstance(instance);
            }
        }

        return intraSchedulerResult;
    }
}
