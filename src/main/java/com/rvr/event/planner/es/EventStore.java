package com.rvr.event.planner.es;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.EventStateRoot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface EventStore<V> {
    EventStream<Long> loadEventStream(UUID aggregateId);

    EventAggregator readEventStateRoot(UUID aggregateIdentifier);

    Optional<EventStateRoot> readSnapshot(UUID aggregateId);

    void appendEvents(EventStateRoot eventStateAggregate, List<Event> events);

    void updateSnapshot(EventStateRoot eventStateAggregate);
}

