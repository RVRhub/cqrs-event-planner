package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import lombok.Getter;

import java.util.UUID;

public class CreateNewEvent implements Event {
    private final UUID eventId;
    @Getter
    private final String member;
    @Getter
    public final long sequenceNumber;

    public CreateNewEvent(UUID eventId, String member, long sequenceNumber) {
        this.eventId = eventId;
        this.member = member;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public UUID aggregateId() {
        return eventId;
    }
}
