package org.lgdcloudsim.intrascheduler;

public class IntraSchedulerFirstFitTest extends IntraSchedulerTestBase<IntraSchedulerFirstFit> {
    @Override
    public IntraSchedulerFirstFit createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new IntraSchedulerFirstFit(id, firstPartitionId, partitionNum);
    }
}
