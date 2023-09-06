package org.cpnsim.innerscheduler;

import org.junit.Ignore;

// TODO: after finish {@link InnerSchedulerMinHostOn}, remove the following Ignore to enable this test
@Ignore
public class InnerSchedulerMinHostOnTest extends InnerSchedulerTestBase<InnerSchedulerMinHostOn> {
    @Override
    public InnerSchedulerMinHostOn createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new InnerSchedulerMinHostOn(id, firstPartitionId, partitionNum);
    }
}