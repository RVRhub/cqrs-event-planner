package com.rvr.event.planner.service;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.processors.EventState;
import com.rvr.event.planner.domain.processors.EventsProjection;
import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.es.memory.InMemoryEventStore;
import com.rvr.event.planner.es.protobuf.ProtobufEventStore;
import com.rvr.event.planner.service.utils.ReflectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QueryCurrentStatusService {
    private final EventStore eventStore;

    @Autowired
    public QueryCurrentStatusService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public EventState getEventStateByEventId(UUID eventId, boolean isInMemoryStorage) {
        EventsProjection eventsProjection = new EventsProjection();
        getEvents(eventId, isInMemoryStorage)
                .forEach(event -> ReflectionUtil.invokeApplyMethod(eventsProjection, event));
        return eventsProjection.get(eventId);
    }

    private List<Event> getEvents(UUID eventId, boolean isInMemoryStorage) {
        if (isInMemoryStorage) {
            return ((InMemoryEventStore) eventStore)
                    .loadEventsAfter()
                    .stream()
                    .filter(event -> event.aggregateId() == eventId)
                    .collect(Collectors.toList());
        }

        return ((ProtobufEventStore) eventStore)
                .loadEventStream(eventId).getEvents();
    }
}
