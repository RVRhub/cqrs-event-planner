package com.rvr.event.planner.domain.processors.handlers;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.event.*;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.EventStateRoot;

import java.util.function.Function;

public class EventHandler extends Handler implements Function<Event, EventStateRoot> {

    private EventStateRoot eventStateAggregate;

    public EventHandler(EventStateRoot eventStateRoot) {
        this.eventStateAggregate = eventStateRoot;
    }

    public EventStateRoot apply(CreateNewEvent e) {
        eventStateAggregate.setState(EventAggregator.State.created);
        this.eventStateAggregate.setPlace(Place.Empty);
        eventStateAggregate.incCounterOfMember();
        return this.eventStateAggregate;
    }

    public EventStateRoot apply(MemberOfferEvent e) {
        if (eventStateAggregate.getState() == EventAggregator.State.created) {
            eventStateAggregate.setState(EventAggregator.State.planning);
            setPlaceWithMaxPriority(e.getPlace(), eventStateAggregate);
        } else if (eventStateAggregate.getState() == EventAggregator.State.planning) {
            setPlaceWithMaxPriority(e.getPlace(), eventStateAggregate);
        }
        eventStateAggregate.incCounterOfMember();
        this.eventStateAggregate.getMembers().add(e.getMember());

        return this.eventStateAggregate;
    }

    public EventStateRoot apply(PlannedEvent e) {
        eventStateAggregate.setState(EventAggregator.State.planned);
        return this.eventStateAggregate;
    }

    public EventStateRoot apply(DeclinedEvent e) {
        eventStateAggregate.setState(EventAggregator.State.declined);
        return this.eventStateAggregate;
    }

    public EventStateRoot apply(OrganizedEvent e) {
        eventStateAggregate.setState(EventAggregator.State.organized);
        return this.eventStateAggregate;
    }

    @Override
    public EventStateRoot apply(Event event) {
        if (event instanceof CreateNewEvent) {
            return apply((CreateNewEvent) event);
        } else if (event instanceof MemberOfferEvent) {
            return apply((MemberOfferEvent) event);
        } else if (event instanceof PlannedEvent) {
            return apply((PlannedEvent) event);
        } else if (event instanceof DeclinedEvent) {
            return apply((DeclinedEvent) event);
        } else if (event instanceof OrganizedEvent) {
            return apply((OrganizedEvent) event);
        }
        return this.eventStateAggregate;
    }
}