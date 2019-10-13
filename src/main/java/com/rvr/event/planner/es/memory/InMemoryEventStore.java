package com.rvr.event.planner.es.memory;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.EventStateRoot;
import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.es.EventStream;
import com.rvr.event.planner.es.ListEventStream;

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

    private void store(UUID aggregateId, long version, List<Event> events) {
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

    @Override
    public void appendEvents(EventStateRoot eventStateAggregate, List<Event> events) {
        store(eventStateAggregate.getAggregateId(), eventStateAggregate.getVersion(), events);
    }

    public EventStream<Long> loadEventsAfter() {
        List<Event> events = new LinkedList<>();
        eventsStore.forEach(events::addAll);
        return new ListEventStream(System.currentTimeMillis(), events);
    }

    public EventStream<Long> loadEventsAfter(Long timestamp) {
        List<Event> events = new LinkedList<>();
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
    public void updateSnapshot(EventStateRoot eventStateAggregate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventAggregator readEventStateRoot(UUID aggregateIdentifier) {
        var eventStream = loadEventStream(aggregateIdentifier);
        var eventAggregator
                = new EventAggregator(aggregateIdentifier, eventStream.version());
        for (Event event : eventStream) {
            eventAggregator.getEventHandler().apply(event);
        }
        eventAggregator.getEventStateAggregate().setVersion(eventStream.version());
        return eventAggregator;
    }

    @Override
    public Optional<EventStateRoot> readSnapshot(UUID aggregateId) {
        return Optional.empty();
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