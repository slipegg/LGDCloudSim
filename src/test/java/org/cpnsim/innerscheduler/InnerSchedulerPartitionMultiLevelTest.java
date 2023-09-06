package org.cpnsim.innerscheduler;

public class InnerSchedulerPartitionMultiLevelTest extends InnerSchedulerTestBase<InnerSchedulerPartitionMultiLevel> {
    @Override
    public InnerSchedulerPartitionMultiLevel createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new InnerSchedulerPartitionMultiLevel(id, firstPartitionId, partitionNum);
    }
}