package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public class RejectEvent implements Event {
    private final UUID aggregateId;
    @Getter
    public final String member;
    @Getter
    public final long sequenceNumber;

    @Override
    public UUID aggregateId() {
        return aggregateId;
    }
}
