package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import lombok.Getter;

import java.util.UUID;

public class CreateNewEvent implements Event {
    private final UUID aggregateId;
    @Getter
    private final String member;
    @Getter
    public final long sequenceNumber;

    public CreateNewEvent(UUID aggregateId, String member, long sequenceNumber) {
        this.aggregateId = aggregateId;
        this.member = member;
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public UUID aggregateId() {
        return aggregateId;
    }
}
