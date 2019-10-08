package com.rvr.event.planner.domain.processors;

import com.rvr.event.planner.domain.Place;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class EventState {
    private UUID eventId;
    private String createdBy;
    private Place place;
    private EventsProjection.State state;
    private Map<String, Place> offers;
}