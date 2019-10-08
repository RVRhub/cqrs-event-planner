package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public class DeclinedEvent implements Event {
    public final UUID aggregateId;
    @Getter
    public final long sequenceNumber;

    @Override
    public UUID aggregateId() {
        return aggregateId;
    }
}
