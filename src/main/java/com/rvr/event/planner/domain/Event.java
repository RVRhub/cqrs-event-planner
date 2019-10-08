package com.rvr.event.planner.domain;

import java.util.UUID;

public interface Event {
    long getSequenceNumber();
    UUID aggregateId();
}
