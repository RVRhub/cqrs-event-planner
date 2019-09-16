package com.rvr.event.planner;

import java.util.UUID;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.command.CreateEventCommand;
import com.rvr.event.planner.domain.command.MakeDecisionCommand;
import com.rvr.event.planner.domain.command.MemberOfferCommand;
import com.rvr.event.planner.domain.event.CreateNewEvent;
import com.rvr.event.planner.domain.event.DeclinedEvent;
import com.rvr.event.planner.domain.event.MemberOfferEvent;
import com.rvr.event.planner.domain.event.PlannedEvent;
import com.rvr.event.planner.domain.processors.EventsProjection;
import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.service.ApplicationService;
import com.rvr.event.planner.es.EventStream;
import com.rvr.event.planner.es.memory.InMemoryEventStore;
import org.junit.Test;

import static org.junit.Assert.*;


public class EventIntegrationTest {
    InMemoryEventStore eventStore = new InMemoryEventStore();
    ApplicationService application = new ApplicationService(eventStore);
    UUID eventId = UUID.randomUUID();
    String player1 = UUID.randomUUID().toString();
    String player2 = UUID.randomUUID().toString();

    @Test
    public void getEventStateByEventIdTest() throws Exception {
        EventsProjection expectedEventsProjection = new EventsProjection();
        expectedEventsProjection.apply(new CreateNewEvent(eventId, player1));
        expectedEventsProjection.apply(new MemberOfferEvent(eventId, player1, Place.CafeOne));
        expectedEventsProjection.apply(new MemberOfferEvent(eventId, player2, Place.CafeOne));
        expectedEventsProjection.apply(new PlannedEvent(eventId, Place.CafeOne));

        application.handle(new CreateEventCommand(eventId, player1));
        application.handle(new MemberOfferCommand(eventId, player1, Place.CafeOne));
        application.handle(new MemberOfferCommand(eventId, player2, Place.CafeOne));
        assertEquals(application.getEventStateByEventId(eventId), expectedEventsProjection.get(eventId));
    }

    @Test
    public void declined() throws Exception {
        application.handle(new CreateEventCommand(eventId, player1));
        application.handle(new MemberOfferCommand(eventId, player1, Place.CafeOne));
        application.handle(new MemberOfferCommand(eventId, player2, Place.CafeOne));
        application.handle(new MakeDecisionCommand(eventId));
        assertEventStreamContains(eventId, new DeclinedEvent(eventId));
    }

    @Test
    public void victory() throws Exception {
        application.handle(new CreateEventCommand(eventId, player1));
        application.handle(new MemberOfferCommand(eventId, player1, Place.CafeOne));
        application.handle(new MemberOfferCommand(eventId, player2, Place.CafeOne));
        application.handle(new MakeDecisionCommand(eventId));
        assertEventStreamContains(eventId, new PlannedEvent(eventId, Place.CafeOne));
    }

//    @Test(expected=IllegalArgumentException.class)
//    public void same_player_should_fail() throws Exception {
//        application.handle(new CreateEventCommand(eventId, player1));
//        application.handle(new MemberOfferCommand(eventId, player1, Place.CafeOne));
//        application.handle(new MemberOfferCommand(eventId, player2, Place.CafeOne));
//        application.handle(new MakeDecisionCommand(eventId));
//    }

    @Test(expected=IllegalStateException.class)
    public void event_not_started() throws Exception {
        application.handle(new MemberOfferCommand(eventId, player1, Place.CafeOne));
    }

    private void assertEventStreamContains(UUID streamId, Event expectedEvent) {
        EventStream<Long> eventStream = eventStore.loadEventStream(eventId);
        String expected = EventStringUtil.toString(expectedEvent);
        for (Event event : eventStream) {
            if (EventStringUtil.toString(event).equals(expected)) return;
        }
        fail("Expected event did not occur: " + expected);
    }
}