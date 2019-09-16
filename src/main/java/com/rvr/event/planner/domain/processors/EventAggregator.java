package com.rvr.event.planner.domain.processors;

import com.google.common.collect.Lists;
import com.rvr.event.planner.domain.Command;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.command.CreateEventCommand;
import com.rvr.event.planner.domain.command.MakeDecisionCommand;
import com.rvr.event.planner.domain.command.MemberOfferCommand;
import com.rvr.event.planner.domain.event.*;
import com.rvr.event.planner.domain.Place;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;

public class EventAggregator {

    enum State {
        notInitalized, created, planning, planned, declined
    }

    private final static int PARTICIPANT_LIMIT = 2;
    private State state = State.notInitalized;
    private String member;
    private int counterOfMember;
    private Place place;
    @Getter
    private EventHandler eventHandler;
    @Getter
    private CommandHandler commandHandler;

    public EventAggregator() {
        this.commandHandler = new CommandHandler();
        this.eventHandler = new EventHandler();
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
            if (state != State.notInitalized) {
                throw new IllegalStateException(state.toString());
            }

            EventAggregator.this.counterOfMember = 0;
            return Arrays.asList(new CreateNewEvent(c.aggregateId(), c.getMember()));
        }

        public List<Event> handle(MemberOfferCommand c) {
            if (State.created == state) {
                counterOfMember++;
                return Arrays.asList(new MemberOfferEvent(c.aggregateId(), c.getMember(), c.getPlace()));
            } else if (State.planning == state) {
                if (member.equals(c.getMember())) {
                    throw new IllegalArgumentException("Member already vote");
                }
                counterOfMember++;
                setPlaceWithMaxPriority(c.getPlace());

                var events = new ArrayList<Event>();
                events.add(new MemberOfferEvent(c.aggregateId(), c.getMember(), c.getPlace()));
                if (counterOfMember >= PARTICIPANT_LIMIT) {
                    events.addAll(getDecisionEvents(c.aggregateId()));
                }
                return events;
            } else {
                throw new IllegalStateException(state.toString());
            }
        }

        public List<Event> handle(MakeDecisionCommand c) {
            return getDecisionEvents(c.aggregateId());
        }

        private List<Event> getDecisionEvents(UUID aggregateId) {
            if (State.planning == state && EventAggregator.this.counterOfMember > 1) {
                if (EventAggregator.this.place.getPriority() > Place.Empty.getPriority()) {
                    return Arrays.asList(new PlannedEvent(aggregateId, EventAggregator.this.place));
                }
            }

            return Arrays.asList(new DeclinedEvent(aggregateId));
        }
    }

    public class EventHandler implements Function<Event, EventAggregator> {

        public EventAggregator apply(CreateNewEvent e) {
            state = State.created;
            EventAggregator.this.place = Place.Empty;
            counterOfMember = 0;
            return EventAggregator.this;
        }

        public EventAggregator apply(MemberOfferEvent e) {
            if (state == State.created) {
                state = State.planning;
                setPlaceWithMaxPriority(e.getPlace());
            } else if (state == State.planning) {
                setPlaceWithMaxPriority(e.getPlace());
            }
            counterOfMember++;
            EventAggregator.this.member = e.getMember();

            return EventAggregator.this;
        }

        public EventAggregator apply(PlannedEvent e) {
            state = State.planned;
            return EventAggregator.this;
        }

        public EventAggregator apply(DeclinedEvent e) {
            state = State.declined;
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
                && place.getPriority() > EventAggregator.this.place.getPriority()) {
            EventAggregator.this.place = place;
        }
    }
}
