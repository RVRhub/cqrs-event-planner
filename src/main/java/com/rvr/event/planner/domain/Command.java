package com.rvr.event.planner.domain;

import java.util.UUID;

public interface Command {
    UUID aggregateId();
}
