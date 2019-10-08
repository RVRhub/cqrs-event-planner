package com.rvr.event.planner.es.protobuf;

import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.event.CreateNewEvent;
import com.rvr.event.planner.domain.event.MemberOfferEvent;
import com.rvr.event.planner.domain.processors.EventStateRoot;
import com.rvr.event.planner.es.ListEventStream;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ProtobufEventStoreTest {

    UUID eventId = UUID.randomUUID();
    String player1 = UUID.randomUUID().toString();
    String player2 = UUID.randomUUID().toString();

    @Test
    public void writeToEventStore() throws Exception {
        // given
        ProtobufEventStore es = new ProtobufEventStore();
        EventStateRoot eventStateAggregate = new EventStateRoot(eventId);
        es.appendEvents(eventStateAggregate,
                Lists.newArrayList(new CreateNewEvent(eventId, player1, 0)));
        eventStateAggregate.setVersion(1);
        es.appendEvents(eventStateAggregate,
                Lists.newArrayList(new MemberOfferEvent(eventId, player2, Place.CafeOne, 1)));

        // when
        ListEventStream eventStream = es.loadEventStream(eventId);

        // than
        assertEquals(eventStream.stream().count(), 2);
    }
}