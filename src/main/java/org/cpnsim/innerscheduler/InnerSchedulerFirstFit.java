package org.cpnsim.innerscheduler;

import org.cpnsim.request.Instance;
import org.cpnsim.statemanager.SynState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InnerSchedulerFirstFit extends InnerSchedulerSimple{
    int lastHostIndex = 0;
    int lastPartitionIndx = 0;
    public InnerSchedulerFirstFit(int id, int firstPartitionId, int partitionNum) {
        super(id, firstPartitionId, partitionNum);
    }

    @Override
    protected InnerSchedulerResult scheduleInstances(List<Instance> instances, SynState synState) {
        InnerSchedulerResult innerSchedulerResult = new InnerSchedulerResult(this, getDatacenter().getSimulation().clock());
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
                innerSchedulerResult.addScheduledInstance(instance);
            } else {
                innerSchedulerResult.addFailedScheduledInstance(instance);
            }
        }

        return innerSchedulerResult;
    }
}
