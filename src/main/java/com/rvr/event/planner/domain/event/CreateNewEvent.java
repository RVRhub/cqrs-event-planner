package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import lombok.Getter;

import java.util.UUID;

public class CreateNewEvent implements Event {
    private final UUID eventId;
    @Getter
    private final String member;

    CreateNewEvent() {
        eventId = null;
        member = null;
    }

    public CreateNewEvent(UUID eventId, String member) {
        this.eventId = eventId;
        this.member = member;
    }

    @Override
    public UUID aggregateId() {
        return eventId;
    }
}
