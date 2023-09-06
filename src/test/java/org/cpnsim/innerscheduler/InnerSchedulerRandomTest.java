package org.cpnsim.innerscheduler;

public class InnerSchedulerRandomTest extends InnerSchedulerTestBase<InnerSchedulerRandom> {
    @Override
    public InnerSchedulerRandom createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new InnerSchedulerRandom(id, firstPartitionId, partitionNum);
    }
}