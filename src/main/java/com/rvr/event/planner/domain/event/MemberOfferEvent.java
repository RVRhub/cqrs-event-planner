package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public class MemberOfferEvent implements Event {
    private final UUID aggregateId;

    @Getter
    private final String member;
    @Getter
    private final Place place;
    @Getter
    public final long sequenceNumber;

    @Override
    public UUID aggregateId() {
        return aggregateId;
    }
}
