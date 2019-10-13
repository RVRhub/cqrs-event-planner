package com.rvr.event.planner.domain.processors.handlers;

import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.processors.EventStateRoot;

public abstract class Handler {
    protected void setPlaceWithMaxPriority(Place place, EventStateRoot eventStateAggregate) {
        if (place != null
                && place.getPriority() > eventStateAggregate.getPlace().getPriority()) {
            eventStateAggregate.setPlace(place);
        }
    }
}
