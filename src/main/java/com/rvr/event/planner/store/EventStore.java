package com.rvr.event.planner.store;

import java.util.List;
import java.util.UUID;

import com.rvr.event.planner.domain.Event;
import rx.Observable;


public interface EventStore<V> {
    EventStream<Long> loadEventStream(UUID aggregateId);

    void store(UUID aggregateId, long version, List<Event> events);

    Observable<Event> all();
}

