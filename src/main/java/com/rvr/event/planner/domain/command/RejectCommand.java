package com.rvr.event.planner.domain.command;

import com.rvr.event.planner.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public class RejectCommand implements Command {

    private final UUID aggregateId;
    @Getter
    private final String member;

    @Override
    public UUID aggregateId() {
        return aggregateId;
    }
}
