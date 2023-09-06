package org.cloudsimplus.core.events;

public class DeferredQueueTest extends EventQueueTestBase<DeferredQueue> {
    public DeferredQueue createEventQueue() {
        return new DeferredQueue();
    }
}
