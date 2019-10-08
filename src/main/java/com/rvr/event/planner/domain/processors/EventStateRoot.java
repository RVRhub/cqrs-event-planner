package com.rvr.event.planner.domain.processors;

import com.rvr.event.planner.domain.Place;
import lombok.Data;

import java.util.UUID;

@Data
public class EventStateRoot {
    private UUID aggregateId;
    private EventAggregator.State state;
    private String member;
    private Place place;
    private long version;
    private int counterOfMember;

    public EventStateRoot(UUID aggregateId) {
        this(aggregateId, EventAggregator.State.notInitalized, 0);
    }

    public EventStateRoot(UUID aggregateId, EventAggregator.State state, long version) {
        this.aggregateId = aggregateId;
        this.state = state;
        this.version = version;
    }

    public void incCounterOfMember() {
        this.counterOfMember++;
    }
}
