package com.rvr.event.planner.web;

import com.rvr.event.planner.domain.Place;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class EventDTO {
    private String eventId;
    private String createdBy;
    private Place place;
    private String state;
    private Map<String, Place> offers;
}
