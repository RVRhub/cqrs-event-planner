package com.rvr.event.planner.domain.event;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import lombok.Getter;

import java.util.UUID;

public class MemberOfferEvent implements Event {
    private final UUID eventId;

    @Getter
    private final String member;
    @Getter
    private final Place place;

    public MemberOfferEvent(UUID eventId, String member, Place place) {
        this.eventId = eventId;
        this.member = member;
        this.place = place;
    }

    public MemberOfferEvent() {
        eventId = null;
        member = null;
        place = null;
    }

    @Override
    public UUID aggregateId() {
        return eventId;
    }
}
