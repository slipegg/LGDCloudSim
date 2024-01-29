package org.cpnsim.intrascheduler;

public class IntraSchedulerPartitionMultiLevelTest extends IntraSchedulerTestBase<IntraSchedulerPartitionMultiLevel> {
    @Override
    public IntraSchedulerPartitionMultiLevel createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new IntraSchedulerPartitionMultiLevel(id, firstPartitionId, partitionNum);
    }
}