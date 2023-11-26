package org.cpnsim.core.events;

public class FutureQueueTest extends EventQueueTestBase<FutureQueue> {
    public FutureQueue createEventQueue() {
        return new FutureQueue();
    }
}
