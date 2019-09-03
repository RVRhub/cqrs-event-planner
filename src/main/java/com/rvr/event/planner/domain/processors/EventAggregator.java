package com.rvr.event.planner.domain.processors;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.command.CreateEventCommand;
import com.rvr.event.planner.domain.command.MakeDecisionCommand;
import com.rvr.event.planner.domain.command.MemberOfferCommand;
import com.rvr.event.planner.domain.event.*;
import com.rvr.event.planner.domain.Place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EventAggregator {
    enum State {
        notInitalized, created, planning, planned, declined
    }

    private final static int PARTICIPANT_LIMIT = 2;
    private State state = State.notInitalized;
    private String member;
    private int counterOfMember;
    private Place place;

    public List<Event> handle(CreateEventCommand c) {
        if (state != State.notInitalized) {
            throw new IllegalStateException(state.toString());
        }

        this.counterOfMember = 0;
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
        if (State.planning == state && this.counterOfMember > 1) {
            if (this.place.getPriority() > Place.Empty.getPriority()) {
                return Arrays.asList(new PlannedEvent(aggregateId, this.place));
            }
        }

        return Arrays.asList(new DeclinedEvent(aggregateId));
    }

    private void setPlaceWithMaxPriority(Place place) {
        if (place != null
                && place.getPriority() > this.place.getPriority()) {
            this.place = place;
        }
    }

    public void apply(CreateNewEvent e) {
        state = State.created;
        this.place = Place.Empty;
        counterOfMember = 0;
    }

    public void apply(MemberOfferEvent e) {
        if (state == State.created) {
            state = State.planning;
            setPlaceWithMaxPriority(e.getPlace());
        } else if (state == State.planning) {
            setPlaceWithMaxPriority(e.getPlace());
        }
        counterOfMember++;
        this.member = e.getMember();
    }

    public void apply(PlannedEvent e) {
        state = State.planned;
    }

    public void apply(DeclinedEvent e) {
        state = State.declined;
    }
}
