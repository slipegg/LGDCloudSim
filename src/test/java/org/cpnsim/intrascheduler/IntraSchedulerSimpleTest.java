package org.cpnsim.intrascheduler;

public class IntraSchedulerSimpleTest extends IntraSchedulerTestBase<IntraSchedulerSimple> {
    @Override
    public IntraSchedulerSimple createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new IntraSchedulerSimple(id, firstPartitionId, partitionNum);
    }
}
