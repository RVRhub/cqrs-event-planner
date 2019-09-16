package com.rvr.event.planner.service;

import com.rvr.event.planner.domain.Command;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.EventsProjection;
import com.rvr.event.planner.service.utils.CommandHandlerLookup;
import com.rvr.event.planner.service.utils.ReflectionUtil;
import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.es.EventStream;
import com.rvr.event.planner.es.memory.InMemoryEventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class ApplicationService {
    private final EventStore eventStore;
    private CommandHandlerLookup commandHandlerLookup;

    @Autowired
    public ApplicationService(EventStore eventStore) {
        this.eventStore = eventStore;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, EventAggregator.class);
    }

    public void handle(Command command) throws Exception {
        EventStream<Long> eventStream = eventStore.loadEventStream(command.aggregateId());
        EventAggregator eventAggregator = new EventAggregator();
        for (Event event : eventStream) {
            eventAggregator = eventAggregator.getEventHandler().apply(event);
        }
        List<Event> events = eventAggregator.getCommandHandler().apply(command);
        if (events != null && events.size() > 0) {
            eventStore.store(command.aggregateId(), eventStream.version(), events);
        } else {
            // Command generated no events Saga
        }
    }

    public EventsProjection.EventState getEventStateByEventId(UUID eventId) {
        EventsProjection eventsProjection = new EventsProjection();
        EventStream<Long> events = ((InMemoryEventStore) eventStore)
                .loadEventsAfter();

        events.stream()
                .filter(event -> eventId.equals(event.aggregateId()))
                .forEach(event -> ReflectionUtil.invokeApplyMethod(eventsProjection, event));
        return eventsProjection.get(eventId);
    }

    @Deprecated
    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException {
        return commandHandlerLookup.targetType(command).newInstance();
    }

}