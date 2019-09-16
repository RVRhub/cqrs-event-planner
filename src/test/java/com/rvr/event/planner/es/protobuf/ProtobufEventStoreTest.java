package com.rvr.event.planner.es.protobuf;

import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.event.CreateNewEvent;
import com.rvr.event.planner.domain.event.MemberOfferEvent;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.UUID;

public class ProtobufEventStoreTest {

    UUID eventId = UUID.randomUUID();
    String player1 = UUID.randomUUID().toString();
    String player2 = UUID.randomUUID().toString();

    @Test
    public void writeToEventStore() throws Exception {
        ProtobufEventStore es = new ProtobufEventStore();
        es.store(eventId, 0,
                Lists.newArrayList(new CreateNewEvent(eventId, player1)));
        es.store(eventId, 0,
                Lists.newArrayList(new MemberOfferEvent(eventId, player2, Place.CafeOne)));

        es.loadEventStream(eventId);
    }
}
