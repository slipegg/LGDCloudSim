package org.cpnsim.intrascheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.SynState;

import java.util.List;

public class IntraSchedulerFirstFit extends IntraSchedulerSimple {
    int lastHostIndex = 0;
    int lastPartitionIndx = 0;

    public IntraSchedulerFirstFit(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    protected IntraSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        IntraSchedulerResult intraSchedulerResult = new IntraSchedulerResult(this, getDatacenter().getSimulation().clock());
        for (Instance instance : instances) {
            int suitId = -1;

            int p = 0;
            for (; p < partitionNum; p++) {
                int[] range = datacenter.getStatesManager().getPartitionRangesManager().getRange(lastPartitionIndx);
                for (int i = range[0]; i <= range[1]; i++) {
                    if (synState.isSuitable(lastHostIndex, instance)) {
                        suitId = lastHostIndex;
                        break;
                    }
                    lastHostIndex = ++lastHostIndex % (range[1] - range[0] + 1);
                }
                if (suitId != -1) {
                    break;
                }
                lastPartitionIndx = ++lastPartitionIndx % partitionNum;
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
