package com.rvr.event.planner.domain.command;

import com.rvr.event.planner.domain.Command;
import lombok.Getter;

import java.util.UUID;

public class CreateEventCommand implements Command {

    private final UUID eventId;
    @Getter
    private final String member;

    public CreateEventCommand(UUID eventId, String member) {
        if (eventId == null || member == null) {
            throw new IllegalArgumentException("EventId/Member must not be null");
        }
        this.eventId = eventId;
        this.member = member;
    }

    @Override
    public UUID aggregateId() {
        return eventId;
    }
}
