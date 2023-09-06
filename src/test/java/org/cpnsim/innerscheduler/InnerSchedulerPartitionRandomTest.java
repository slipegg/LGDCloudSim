package org.cpnsim.innerscheduler;

public class InnerSchedulerPartitionRandomTest extends InnerSchedulerTestBase<InnerSchedulerPartitionRandom> {
    @Override
    public InnerSchedulerPartitionRandom createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new InnerSchedulerPartitionRandom(id, firstPartitionId, partitionNum);
    }
}
