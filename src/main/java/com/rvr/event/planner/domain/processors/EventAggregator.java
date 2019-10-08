package com.rvr.event.planner.domain.processors;

import com.google.common.collect.Lists;
import com.rvr.event.planner.domain.Command;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.command.CreateEventCommand;
import com.rvr.event.planner.domain.command.MakeDecisionCommand;
import com.rvr.event.planner.domain.command.MemberOfferCommand;
import com.rvr.event.planner.domain.event.CreateNewEvent;
import com.rvr.event.planner.domain.event.DeclinedEvent;
import com.rvr.event.planner.domain.event.MemberOfferEvent;
import com.rvr.event.planner.domain.event.PlannedEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class EventAggregator {

    public enum State {
        notInitalized, created, planning, planned, declined
    }

    private final static int PARTICIPANT_LIMIT = 2;
    private EventStateRoot eventStateAggregate;
    private long sequenceNumber;

    @Getter
    private EventHandler eventHandler;
    @Getter
    private CommandHandler commandHandler;

    public EventAggregator(UUID aggregateId, long sequenceNumber) {
        this.eventStateAggregate = new EventStateRoot(aggregateId);
        this.commandHandler = new CommandHandler();
        this.eventHandler = new EventHandler();
        this.sequenceNumber = sequenceNumber;
    }

    public class CommandHandler implements Function<Command, List<Event>> {

        @Override
        public List<Event> apply(Command command) {
            if (command instanceof CreateEventCommand) {
                return handle((CreateEventCommand) command);
            } else if (command instanceof MemberOfferCommand) {
                return handle((MemberOfferCommand) command);
            } else if (command instanceof MakeDecisionCommand) {
                return handle((MakeDecisionCommand) command);
            }
            return Lists.newArrayList();
        }

        public List<Event> handle(CreateEventCommand c) {
            if (eventStateAggregate.getState() != State.notInitalized) {
                throw new IllegalStateException(eventStateAggregate.getState().toString());
            }

            EventAggregator.this.eventStateAggregate.setCounterOfMember(0);
            return Arrays.asList(new CreateNewEvent(c.aggregateId(), c.getMember(), sequenceNumber++));
        }

        public List<Event> handle(MemberOfferCommand c) {
            if (State.created == eventStateAggregate.getState()) {
                eventStateAggregate.incCounterOfMember();
                return Arrays
                        .asList(new MemberOfferEvent(c.aggregateId(), c.getMember(), c.getPlace(), sequenceNumber++));
            } else if (State.planning == eventStateAggregate.getState()) {
                if (eventStateAggregate.getMember().equals(c.getMember())) {
                    throw new IllegalArgumentException("Member already vote");
                }
                eventStateAggregate.incCounterOfMember();
                setPlaceWithMaxPriority(c.getPlace());

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

        public List<Event> handle(MakeDecisionCommand c) {
            return getDecisionEvents(c.aggregateId());
        }

        private List<Event> getDecisionEvents(UUID aggregateId) {
            if (State.planning == eventStateAggregate.getState()
                    && EventAggregator.this.eventStateAggregate.getCounterOfMember() > 1) {
                if (eventStateAggregate.getPlace().getPriority() > Place.Empty.getPriority()) {
                    var plannedEvent = new PlannedEvent(aggregateId, eventStateAggregate.getPlace(), sequenceNumber++);
                    return Arrays.asList(plannedEvent);
                }
            }

            return Arrays.asList(new DeclinedEvent(aggregateId, sequenceNumber++));
        }
    }

    public class EventHandler implements Function<Event, EventAggregator> {

        public EventAggregator apply(CreateNewEvent e) {
            eventStateAggregate.setState(State.created);
            EventAggregator.this.eventStateAggregate.setPlace(Place.Empty);
            eventStateAggregate.incCounterOfMember();
            return EventAggregator.this;
        }

        public EventAggregator apply(MemberOfferEvent e) {
            if (eventStateAggregate.getState() == State.created) {
                eventStateAggregate.setState(State.planning);
                setPlaceWithMaxPriority(e.getPlace());
            } else if (eventStateAggregate.getState() == State.planning) {
                setPlaceWithMaxPriority(e.getPlace());
            }
            eventStateAggregate.incCounterOfMember();
            EventAggregator.this.eventStateAggregate.setMember(e.getMember());

            return EventAggregator.this;
        }

        public EventAggregator apply(PlannedEvent e) {
            eventStateAggregate.setState(State.planned);
            return EventAggregator.this;
        }

        public EventAggregator apply(DeclinedEvent e) {
            eventStateAggregate.setState(State.declined);
            return EventAggregator.this;
        }

        @Override
        public EventAggregator apply(Event event) {
            if (event instanceof CreateNewEvent) {
                return apply((CreateNewEvent) event);
            } else if (event instanceof MemberOfferEvent) {
                return apply((MemberOfferEvent) event);
            } else if (event instanceof PlannedEvent) {
                return apply((PlannedEvent) event);
            } else if (event instanceof DeclinedEvent) {
                return apply((DeclinedEvent) event);
            }
            return EventAggregator.this;
        }
    }

    private void setPlaceWithMaxPriority(Place place) {
        if (place != null
                && place.getPriority() > EventAggregator.this.eventStateAggregate.getPlace().getPriority()) {
            EventAggregator.this.eventStateAggregate.setPlace(place);
        }
    }

    public EventStateRoot getEventStateAggregate() {
        return eventStateAggregate;
    }
}
