package org.lgdcloudsim.core.events;

public class DeferredQueueTest extends EventQueueTestBase<DeferredQueue> {
    public DeferredQueue createEventQueue() {
        return new DeferredQueue();
    }
}
