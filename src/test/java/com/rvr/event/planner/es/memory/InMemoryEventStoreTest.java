package com.rvr.event.planner.es.memory;


import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.event.DeclinedEvent;
import com.rvr.event.planner.domain.processors.EventStateRoot;
import com.rvr.event.planner.es.EventStream;
import org.junit.Test;


public class InMemoryEventStoreTest {
    UUID eventId = UUID.randomUUID();

    @Test
    public void test() throws Exception {
        InMemoryEventStore es = new InMemoryEventStore();
        EventStateRoot eventStateAggregate = new EventStateRoot(eventId);
        es.appendEvents(eventStateAggregate, Arrays.asList(new DeclinedEvent(eventId, 0)));
        Thread.sleep(1);
        eventStateAggregate.setVersion(1);
        es.appendEvents(eventStateAggregate, Arrays.asList(new DeclinedEvent(eventId, 1)));
        EventStream<Long> stream = es.loadEventsAfter(0L);
        assertEquals(1, countEvents(stream));
        Long id = stream.version();
        System.out.println("id=" + id);
    }

    @Test
    public void testWithFullListOfEvents() throws Exception {
        InMemoryEventStore es = new InMemoryEventStore();
        EventStateRoot eventStateAggregate = new EventStateRoot(eventId);
        es.appendEvents(eventStateAggregate, Arrays.asList(new DeclinedEvent(eventId, 0)));
        eventStateAggregate.setVersion(1);
        es.appendEvents(eventStateAggregate, Arrays.asList(new DeclinedEvent(eventId, 1)));
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