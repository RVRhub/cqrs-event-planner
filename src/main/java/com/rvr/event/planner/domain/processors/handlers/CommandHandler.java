package com.rvr.event.planner.domain.processors.handlers;

import com.google.common.collect.Lists;
import com.rvr.event.planner.domain.Command;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.command.*;
import com.rvr.event.planner.domain.event.*;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.EventStateRoot;
import com.rvr.event.planner.service.SagaService;

import java.util.*;
import java.util.function.Function;

public class CommandHandler extends Handler implements Function<Command, List<Event>> {

    private final static int PARTICIPANT_LIMIT = 2;
    private long sequenceNumber;

    private EventStateRoot eventStateAggregate;
    private SagaService sagaService;

    public CommandHandler(EventStateRoot eventStateRoot, long sequenceNumber) {
        this.eventStateAggregate = eventStateRoot;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public List<Event> apply(Command command) {
        if (command instanceof CreateEventCommand) {
            return handle((CreateEventCommand) command);
        } else if (command instanceof MemberOfferCommand) {
            return handle((MemberOfferCommand) command);
        } else if (command instanceof MakeDecisionCommand) {
            return handle((MakeDecisionCommand) command);
        } else if (command instanceof AcceptCommand) {
            return handle((AcceptCommand) command);
        } else if (command instanceof RejectCommand) {
            return handle((RejectCommand) command);
        } else if (command instanceof AddParticipantsCommand) {
            return handle((AddParticipantsCommand) command);
        }
        return Lists.newArrayList();
    }

    public List<Event> handle(CreateEventCommand c) {
        if (eventStateAggregate.getState() != EventAggregator.State.notInitalized) {
            throw new IllegalStateException(eventStateAggregate.getState().toString());
        }

        this.eventStateAggregate.setCounterOfMember(0);
        return Arrays.asList(new CreateNewEvent(c.aggregateId(), c.getMember(), sequenceNumber++));
    }

    public List<Event> handle(MemberOfferCommand c) {
        if (EventAggregator.State.created == eventStateAggregate.getState()) {
            eventStateAggregate.incCounterOfMember();
            return Arrays
                    .asList(new MemberOfferEvent(c.aggregateId(), c.getMember(), c.getPlace(), sequenceNumber++));
        } else if (EventAggregator.State.planning == eventStateAggregate.getState()) {
            if (eventStateAggregate.getMembers().contains(c.getMember())) {
                throw new IllegalArgumentException("Member already vote");
            }
            eventStateAggregate.incCounterOfMember();
            setPlaceWithMaxPriority(c.getPlace(), eventStateAggregate);

            var events = new ArrayList<Event>();
            events.add(new MemberOfferEvent(c.aggregateId(), c.getMember(), c.getPlace(), sequenceNumber++));
            if (eventStateAggregate.getCounterOfMember() >= PARTICIPANT_LIMIT) {
                events.addAll(getDecisionEvents(c.aggregateId()));
            }
            return events;
        } else {
            throw new IllegalStateException(eventStateAggregate.getState().toString());
        }
    }

    public List<Event> handle(AcceptCommand c) {
        if (EventAggregator.State.planned == eventStateAggregate.getState()) {
            eventStateAggregate.incCounterOfMember();
            return Collections.singletonList(new AcceptEvent(c.aggregateId(), c.getMember(), sequenceNumber++));
        } else {
            throw new IllegalStateException(eventStateAggregate.getState().toString());
        }
    }

    public List<Event> handle(AddParticipantsCommand c) {
        if (EventAggregator.State.created == eventStateAggregate.getState()) {
            eventStateAggregate.incCounterOfMember();
            eventStateAggregate.getMembers().addAll(c.getMembers());
            return Arrays
                    .asList(new AddParticipantsEvent(c.aggregateId(), c.getMembers(), sequenceNumber++));
        } else {
            throw new IllegalStateException(eventStateAggregate.getState().toString());
        }
    }

    public List<Event> handle(RejectCommand c) {
        if (EventAggregator.State.planned == eventStateAggregate.getState()) {
            eventStateAggregate.incCounterOfMember();
            return Arrays
                    .asList(new RejectEvent(c.aggregateId(), c.getMember(), sequenceNumber++));
        } else {
            throw new IllegalStateException(eventStateAggregate.getState().toString());
        }
    }

    public List<Event> handle(MakeDecisionCommand c) {
        return getDecisionEvents(c.aggregateId());
    }

    private List<Event> getDecisionEvents(UUID aggregateId) {
        if (isCanBePlanned() && isOfferedPlaceNotEmpty()) {
            var plannedEvent = new PlannedEvent(aggregateId, eventStateAggregate.getPlace(), sequenceNumber++);
            return Arrays.asList(plannedEvent);
        }

        return Arrays.asList(new DeclinedEvent(aggregateId, sequenceNumber++));
    }

    private boolean isOfferedPlaceNotEmpty() {
        return eventStateAggregate.getPlace().getPriority() > Place.Empty.getPriority();
    }

    private boolean isCanBePlanned() {
        return EventAggregator.State.planning == eventStateAggregate.getState()
                && this.eventStateAggregate.getCounterOfMember() > 1;
    }
}
