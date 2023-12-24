package org.cpnsim.intrascheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.HostState;
import org.cpnsim.statemanager.SynState;

import java.util.*;

public class IntraSchedulerPartitionRandom extends IntraSchedulerSimple {
    Random random = new Random();
    long excludeTimeNanos = 0;

    public IntraSchedulerPartitionRandom(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        excludeTimeNanos = 0;
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());

        int synPartitionId = firstPartitionId;
        if (datacenter.getStatesManager().isSynCostTime()) {
            synPartitionId = (firstPartitionId + datacenter.getStatesManager().getSmallSynGapCount()) % partitionNum;
        }
        for (Instance instance : instances) {
            int suitId = -1;

            for (int p = 0; p < partitionNum; p++) {
                int[] range = datacenter.getStatesManager().getPartitionRangesManager().getRange((synPartitionId + partitionNum - p) % partitionNum);
                int startHostId = random.nextInt(range[1] - range[0] + 1);
                int rangeLength = range[1] - range[0] + 1;
                for (int i = 0; i < rangeLength; i++) {
                    int hostId = range[0] + (startHostId + i) % rangeLength;
                    long startTime = System.nanoTime();
                    HostState hostState = synState.getHostState(hostId);
                    long endTime  = System.nanoTime();
                    excludeTimeNanos += endTime - startTime;
                    if (hostState.isSuitable(instance)) {
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
                instance.setExpectedScheduleHostId(suitId);
                intraSchedulerResult.addScheduledInstance(instance);
            } else {
                intraSchedulerResult.addFailedScheduledInstance(instance);
            }
        }

        excludeTime = excludeTimeNanos/1_000_000;
        return intraSchedulerResult;
    }
}
