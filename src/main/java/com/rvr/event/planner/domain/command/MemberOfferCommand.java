package com.rvr.event.planner.domain.command;

import com.rvr.event.planner.domain.Command;
import com.rvr.event.planner.domain.Place;
import lombok.Getter;

import java.util.UUID;

public class MemberOfferCommand implements Command {

    private final UUID eventId;
    @Getter
    private final String member;
    @Getter
    private final Place place;

    public MemberOfferCommand(UUID eventId, String member, Place place) {
        if (eventId == null || member == null) {
            throw new IllegalArgumentException("EventId/Member must not be null");
        }
        if (place == null) {
            throw new IllegalArgumentException("Place must not be null");
        }
        this.eventId = eventId;
        this.member = member;
        this.place = place;

    }

    @Override
    public UUID aggregateId() {
        return eventId;
    }
}
