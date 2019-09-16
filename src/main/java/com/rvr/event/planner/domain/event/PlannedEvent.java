package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import lombok.Getter;

import java.util.UUID;

public class PlannedEvent implements Event {
    private final UUID eventId;
    @Getter
    private final Place place;

    public PlannedEvent(UUID eventId, Place place) {
        this.eventId = eventId;
        this.place = place;
    }

    public PlannedEvent() {
        eventId = null;
        place = null;
    }

    @Override
    public UUID aggregateId() {
        return eventId;
    }
}
