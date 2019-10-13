package com.rvr.event.planner.domain.command;

import com.rvr.event.planner.domain.Command;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class AddParticipantsCommand implements Command {

    private final UUID aggregateId;
    @Getter
    private final List<String> members;

    public AddParticipantsCommand(UUID aggregateId, List<String> members) {
        if (aggregateId == null || members == null) {
            throw new IllegalArgumentException("EventId/Member must not be null");
        }
        this.aggregateId = aggregateId;
        this.members = members;
    }

    @Override
    public UUID aggregateId() {
        return aggregateId;
    }
}
