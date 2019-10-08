package com.rvr.event.planner.domain.processors;

import com.rvr.event.planner.domain.event.CreateNewEvent;
import com.rvr.event.planner.domain.event.DeclinedEvent;
import com.rvr.event.planner.domain.event.MemberOfferEvent;
import com.rvr.event.planner.domain.event.PlannedEvent;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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

    private Map<UUID, EventState> events = new HashMap<>();

    public EventState get(UUID eventId) {
        return events.get(eventId);
    }

    public void apply(CreateNewEvent e) {
        EventState event = new EventState();
        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        event.setCreatedBy(DATE_TIME_FORMATTER.format(new Date().toInstant()));
        event.setEventId(e.aggregateId());
        event.setState(State.inProgress);
        event.setOffers(new HashMap<>());
        events.put(e.aggregateId(), event);
    }

    public void apply(MemberOfferEvent e) {
        EventState event = events.get(e.aggregateId());
        event.getOffers().put(e.getMember(), e.getPlace());
        events.put(e.aggregateId(), event);
    }

    public void apply(PlannedEvent e) {
        EventState event = events.get(e.aggregateId());
        event.setState(State.planned);
        event.setPlace(e.getPlace());
    }

    public void apply(DeclinedEvent e) {
        EventState event = events.get(e.aggregateId());
        event.setState(State.declined);
    }
}