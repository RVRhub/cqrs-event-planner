package com.rvr.event.planner.domain.command;

import com.rvr.event.planner.domain.Command;

import java.util.UUID;

public class MakeDecisionCommand implements Command {

    private final UUID eventId;

    public MakeDecisionCommand(UUID eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("EventId must not be null");
        }
        this.eventId = eventId;
    }

    @Override
    public UUID aggregateId() {
        return eventId;
    }
}
