package org.cpnsim.intrascheduler;

public class IntraSchedulerPartitionRandomTest extends IntraSchedulerTestBase<IntraSchedulerPartitionRandom> {
    @Override
    public IntraSchedulerPartitionRandom createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new IntraSchedulerPartitionRandom(id, firstPartitionId, partitionNum);
    }
}
