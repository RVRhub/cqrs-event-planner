package com.rvr.event.planner.domain.processors;

import com.google.common.collect.Lists;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.event.AcceptEvent;
import com.rvr.event.planner.domain.event.DeclinedEvent;
import com.rvr.event.planner.domain.event.OrganizedEvent;
import com.rvr.event.planner.domain.processors.handlers.CommandHandler;
import com.rvr.event.planner.domain.processors.handlers.EventHandler;
import com.rvr.event.planner.domain.processors.saga.PlannedEventSaga;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventAggregator {

    public enum State {
        notInitalized, created, planning, planned, declined, organized
    }

    @Getter
    private EventStateRoot eventStateAggregate;
    @Getter
    private EventHandler eventHandler;
    @Getter
    private CommandHandler commandHandler;

    private long sequenceNumber;

    public EventAggregator(UUID aggregateId, long sequenceNumber) {
        this.eventStateAggregate = new EventStateRoot(aggregateId);
        this.commandHandler = new CommandHandler(this.eventStateAggregate, sequenceNumber);
        this.eventHandler = new EventHandler(eventStateAggregate);
        this.sequenceNumber = sequenceNumber;
    }

    public List<Event> resolve(PlannedEventSaga saga) {
        List<String> acceptEvent = saga.getEvents().stream()
                .filter(t -> t.getClass().isAssignableFrom(AcceptEvent.class))
                .map(t -> ((AcceptEvent) t).getMember())
                .collect(Collectors.toList());

        if (acceptEvent.size() > 1) {
            return Arrays.asList(
                    new OrganizedEvent(UUID.fromString(saga.getKey()), acceptEvent, sequenceNumber++));
        }
        return Lists.newArrayList();
    }

    public List<Event> reject(PlannedEventSaga saga) {
        boolean isCanBeOrganized = saga.getEvents().stream()
                .filter(t -> t.getClass().isAssignableFrom(AcceptEvent.class))
                .count() <= 1;
        if (isCanBeOrganized) {
            return Arrays.asList(
                    new DeclinedEvent(UUID.fromString(saga.getKey()), sequenceNumber++));
        }
        return Lists.newArrayList();
    }
}
