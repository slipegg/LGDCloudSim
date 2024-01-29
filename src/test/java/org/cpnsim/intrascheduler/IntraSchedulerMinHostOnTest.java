package org.cpnsim.intrascheduler;

import org.junit.Ignore;

// TODO: after finish {@link InnerSchedulerMinHostOn}, remove the following Ignore to enable this test
@Ignore
public class IntraSchedulerMinHostOnTest extends IntraSchedulerTestBase<IntraSchedulerMinHostOn> {
    @Override
    public IntraSchedulerMinHostOn createInnerScheduler(int id, int firstPartitionId, int partitionNum) {
        return new IntraSchedulerMinHostOn(id, firstPartitionId, partitionNum);
    }
}