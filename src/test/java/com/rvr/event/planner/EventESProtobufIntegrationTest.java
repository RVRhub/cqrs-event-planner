package com.rvr.event.planner;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.command.*;
import com.rvr.event.planner.domain.event.*;
import com.rvr.event.planner.domain.processors.EventsProjection;
import com.rvr.event.planner.es.EventStream;
import com.rvr.event.planner.es.SagaRepository;
import com.rvr.event.planner.es.protobuf.ProtobufEventStore;
import com.rvr.event.planner.service.CommandHandlerService;
import com.rvr.event.planner.service.QueryCurrentStatusService;
import com.rvr.event.planner.service.SagaService;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class EventESProtobufIntegrationTest {
    ProtobufEventStore eventStore = new ProtobufEventStore();
    SagaRepository sagaRepository = new SagaRepository();
    SagaService sagaService = new SagaService(sagaRepository);
    CommandHandlerService commandHandlerService = new CommandHandlerService(eventStore, sagaService);
    QueryCurrentStatusService queryCurrentStatusService = new QueryCurrentStatusService(eventStore);
    UUID eventId = UUID.randomUUID();
    String player1 = UUID.randomUUID().toString();
    String player2 = UUID.randomUUID().toString();
    String player3 = UUID.randomUUID().toString();

    @Test
    public void getEventStateByEventIdTest() throws Exception {
        EventsProjection expectedEventsProjection = new EventsProjection();
        expectedEventsProjection.apply(new CreateNewEvent(eventId, player1, 0));
        expectedEventsProjection.apply(new MemberOfferEvent(eventId, player1, Place.CafeOne, 1));
        expectedEventsProjection.apply(new MemberOfferEvent(eventId, player2, Place.CafeOne, 2));
        expectedEventsProjection.apply(new PlannedEvent(eventId, Place.CafeOne, 3));
        expectedEventsProjection.apply(new OrganizedEvent(eventId, List.of(player1, player3), 4));

        commandHandlerService
                .handle(new CreateEventCommand(eventId, player1));
        commandHandlerService
                .handle(new AddParticipantsCommand(eventId, List.of(player3)));
        commandHandlerService
                .handle(new MemberOfferCommand(eventId, player1, Place.CafeOne));
        commandHandlerService
                .handle(new MemberOfferCommand(eventId, player2, Place.CafeOne));
        // start saga
        commandHandlerService
                .handle(new AcceptCommand(eventId, player1));
        commandHandlerService
                .handle(new AcceptCommand(eventId, player3));
        // end saga
        assertEquals(queryCurrentStatusService.getEventStateByEventId(eventId, false), expectedEventsProjection.get(eventId));
    }

    @Test
    public void declined() throws Exception {
        commandHandlerService
                .handle(new CreateEventCommand(eventId, player1));
        commandHandlerService
                .handle(new MemberOfferCommand(eventId, player1, Place.Empty));
        commandHandlerService
                .handle(new MakeDecisionCommand(eventId));
        assertEventStreamContains(eventId, new DeclinedEvent(eventId, 2));
    }

    @Test
    public void victory() throws Exception {
        commandHandlerService
                .handle(new CreateEventCommand(eventId, player1));
        commandHandlerService
                .handle(new MemberOfferCommand(eventId, player1, Place.CafeOne));
        commandHandlerService
                .handle(new MemberOfferCommand(eventId, player2, Place.CafeOne));
        commandHandlerService
                .handle(new MakeDecisionCommand(eventId));
        assertEventStreamContains(eventId, new PlannedEvent(eventId, Place.CafeOne, 3));
    }

    private void assertEventStreamContains(UUID streamId, Event expectedEvent) {
        EventStream<Long> eventStream = eventStore.loadEventStream(streamId);
        String expected = EventStringUtil.toString(expectedEvent);
        for (Event event : eventStream) {
            if (EventStringUtil.toString(event).equals(expected)) return;
        }
        fail("Expected event did not occur: " + expected);
    }
}