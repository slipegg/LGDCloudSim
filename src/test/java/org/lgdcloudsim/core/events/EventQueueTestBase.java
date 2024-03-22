package org.lgdcloudsim.core.events;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import lombok.Getter;
import lombok.Setter;

/**
 * Base test for all implementations of {@link EventQueue}
 */
public abstract class EventQueueTestBase<T extends EventQueue> {
    private T queue;

    protected abstract T createEventQueue();

    @Before
    public void setup() {
        this.queue = createEventQueue();
    }

    private SimEvent item(double time) {
        return new FakeSimEvent(time);
    }

    @Test
    public void testAddEvent() {
        assertEquals(0, queue.size());
        List<Double> timeList = List.of(4.0, 2.0, 1.0, 3.0);
        for (double time : timeList) {
            queue.addEvent(item(time));
        }
        assertEquals(timeList.stream().sorted().collect(Collectors.toList()),
                queue.stream().map(SimEvent::getTime).collect(Collectors.toList()));
        return;
    }
}

class FakeSimEvent extends SimEventNull {
    @Getter
    @Setter
    double time;

    FakeSimEvent(double time) {
        this.time = time;
    }

    @Override
    public int compareTo(final SimEvent that) {
        if (that == null || that == NULL) {
            return 1;
        }
        if (this == that) {
            return 0;
        }
        return Double.compare(time, that.getTime());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final CloudSimEvent that = (CloudSimEvent) obj;
        return Double.compare(that.getTime(), getTime()) == 0;
    }
}