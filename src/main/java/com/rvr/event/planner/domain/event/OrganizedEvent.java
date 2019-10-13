package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class OrganizedEvent implements Event {
    private final UUID aggregateId;
    @Getter
    public final List<String> members;
    @Getter
    public final long sequenceNumber;

    @Override
    public UUID aggregateId() {
        return aggregateId;
    }
}
