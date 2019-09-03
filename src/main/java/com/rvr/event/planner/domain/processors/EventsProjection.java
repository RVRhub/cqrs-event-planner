package com.rvr.event.planner.domain.processors;

import com.rvr.event.planner.domain.event.*;
import com.rvr.event.planner.domain.Place;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class EventsProjection {
    public static enum State {
        inProgress(false),
        planned(true),
        declined(true);

        public final boolean completed;

        private State(boolean completed) {
            this.completed = completed;
        }
    }

    @Data
    public static class EventState {
        private UUID eventId;
        private String createdBy;
        private Place place;
        private State state;
        private Map<String, Place> offers;
    }

    private Map<UUID, EventState> events = new HashMap<>();

    public EventState get(UUID eventId) {
        return events.get(eventId);
    }

    public void apply(CreateNewEvent e) {
        EventState event = new EventState();
        Instant instant = Clock.system(ZoneId.of("Europe/Kiev")).instant();
        event.setCreatedBy(instant.toString());
        event.setEventId(e.aggregateId());
        event.setState(State.inProgress);
        event.setOffers(new HashMap<>());
        events.put(e.aggregateId(), event);
    }

    public void apply(MemberOfferEvent e) {
        EventState event = events.get(e.aggregateId());
        event.offers.put(e.getMember(), e.getPlace());
        events.put(e.aggregateId(), event);
    }

    public void apply(PlannedEvent e) {
        EventState event = events.get(e.aggregateId());
        event.state = State.planned;
        event.place = e.place;
    }

    public void apply(DeclinedEvent e) {
        EventState event = events.get(e.aggregateId());
        event.state = State.declined;
    }
}