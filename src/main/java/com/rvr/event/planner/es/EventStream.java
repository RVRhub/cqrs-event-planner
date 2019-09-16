package com.rvr.event.planner.es;

import com.rvr.event.planner.domain.Event;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface EventStream<V> extends Iterable<Event> {
    V version();

    default Stream<Event> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
