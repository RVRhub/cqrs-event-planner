package com.rvr.event.planner.es.memory;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.es.EventStream;
import com.rvr.event.planner.es.ListEventStream;
import rx.Observable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class InMemoryEventStore implements EventStore<Long> {
    private final Map<UUID, ListEventStream> streams = new ConcurrentHashMap<>();
    private final TreeSet<Transaction> transactions = new TreeSet<>();
    private final CopyOnWriteArrayList<List<Event>> eventsStore = new CopyOnWriteArrayList<>();

    @Override
    public ListEventStream loadEventStream(UUID aggregateId) {
        ListEventStream eventStream = streams.get(aggregateId);
        if (eventStream == null) {
            eventStream = new ListEventStream();
            streams.put(aggregateId, eventStream);
        }
        return eventStream;
    }

    @Override
    public void store(UUID aggregateId, long version, List<Event> events) {
        ListEventStream stream = loadEventStream(aggregateId);
        if (stream.version() != version) {
            throw new ConcurrentModificationException("Stream has already been modified");
        }
        streams.put(aggregateId, stream.append(events));
        eventsStore.add(events);
        synchronized (transactions) {
            transactions.add(new Transaction(events));
        }
    }

    public EventStream<Long> loadEventsAfter() {
        List<Event> events = new LinkedList<>();
        eventsStore.forEach(events::addAll);
        return new ListEventStream(System.currentTimeMillis(), events);
    }

    public EventStream<Long> loadEventsAfter(Long timestamp) {
        // include all events after this timestamp, except the events with the current timestamp
        // since new events might be added with the current timestamp
        List<Event> events = new LinkedList<Event>();
        long now;
        synchronized (transactions) {
            now = System.currentTimeMillis();
            for (Transaction t : transactions.tailSet(new Transaction(timestamp)).headSet(new Transaction(now))) {
                events.addAll(t.events);
            }
        }
        return new ListEventStream(now - 1, events);
    }


    @Override
    public Observable<Event> all() {
        throw new UnsupportedOperationException();
    }

    class Transaction implements Comparable<Transaction> {
        public final List<? extends Event> events;
        private final long timestamp;

        public Transaction(long timestamp) {
            events = Collections.emptyList();
            this.timestamp = timestamp;

        }

        public Transaction(List<? extends Event> events) {
            this.events = events;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public int compareTo(Transaction other) {
            if (timestamp < other.timestamp) {
                return -1;
            } else if (timestamp > other.timestamp) {
                return 1;
            }
            return 0;
        }
    }
}