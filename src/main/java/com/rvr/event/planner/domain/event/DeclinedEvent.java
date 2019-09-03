package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;

import java.util.UUID;

public class DeclinedEvent implements Event {
    public final UUID event;

    DeclinedEvent() {
        event = null;
    }

    public DeclinedEvent(UUID eventId) {
        this.event = eventId;
    }

    @Override
    public UUID aggregateId() {
        return event;
    }
}
