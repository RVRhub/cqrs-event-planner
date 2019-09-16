package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;

import java.util.UUID;

public class DeclinedEvent implements Event {
    public final UUID eventId;

    DeclinedEvent() {
        eventId = null;
    }

    public DeclinedEvent(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    public UUID aggregateId() {
        return eventId;
    }
}
