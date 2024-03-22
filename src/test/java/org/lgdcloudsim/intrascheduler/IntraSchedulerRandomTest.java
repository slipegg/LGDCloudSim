package org.lgdcloudsim.intrascheduler;

public class IntraSchedulerRandomTest extends IntraSchedulerTestBase<IntraSchedulerRandom> {
    @Override
    public IntraSchedulerRandom createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new IntraSchedulerRandom(id, firstPartitionId, partitionNum);
    }
}