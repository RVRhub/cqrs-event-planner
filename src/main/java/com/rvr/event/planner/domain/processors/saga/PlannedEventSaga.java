package com.rvr.event.planner.domain.processors.saga;

import com.rvr.event.planner.domain.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class PlannedEventSaga {

    private String key;

    private String handler;

    private List<Class> requiredEvents;

    private Class rejectionEvent;

    private final List<Event> events = new ArrayList<>();

    @Setter
    private boolean isDone;
}
