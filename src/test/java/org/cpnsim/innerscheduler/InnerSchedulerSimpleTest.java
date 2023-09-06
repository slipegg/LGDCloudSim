package org.cpnsim.innerscheduler;

public class InnerSchedulerSimpleTest extends InnerSchedulerTestBase<InnerSchedulerSimple> {
    @Override
    public InnerSchedulerSimple createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new InnerSchedulerSimple(id, firstPartitionId, partitionNum);
    }
}
