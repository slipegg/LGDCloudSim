package org.cpnsim.innerscheduler;

public class InnerSchedulerFirstFitTest extends InnerSchedulerTestBase<InnerSchedulerFirstFit> {
    @Override
    public InnerSchedulerFirstFit createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new InnerSchedulerFirstFit(id, firstPartitionId, partitionNum);
    }
}
