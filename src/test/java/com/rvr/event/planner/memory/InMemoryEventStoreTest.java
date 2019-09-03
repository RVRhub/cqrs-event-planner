package com.rvr.event.planner.memory;


import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.event.DeclinedEvent;
import com.rvr.event.planner.store.EventStream;
import com.rvr.event.planner.store.memory.InMemoryEventStore;
import org.junit.Test;


public class InMemoryEventStoreTest {
    UUID eventId = UUID.randomUUID();

    @Test
    public void test() throws Exception {
        InMemoryEventStore es = new InMemoryEventStore();
        es.store(eventId, 0, Arrays.asList(new DeclinedEvent(eventId)));
        Thread.sleep(1);
        es.store(eventId, 1, Arrays.asList(new DeclinedEvent(eventId)));
        EventStream<Long> stream = es.loadEventsAfter(0L);
        assertEquals(1, countEvents(stream));
        Long id = stream.version();
        System.out.println("id=" + id);
    }

    @Test
    public void testWithFullListOfEvents() throws Exception {
        InMemoryEventStore es = new InMemoryEventStore();
        es.store(eventId, 0, Arrays.asList(new DeclinedEvent(eventId)));
        es.store(eventId, 1, Arrays.asList(new DeclinedEvent(eventId)));
        EventStream<Long> stream = es.loadEventsAfter();
        assertEquals(2, countEvents(stream));
        Long id = stream.version();
        System.out.println("id=" + id);
    }

    private int countEvents(EventStream<Long> stream) {
        int result = 0;
        for (Event event : stream) {
            result++;
        }
        return result;
    }

}