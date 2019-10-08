package com.rvr.event.planner.es.protobuf.converter;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.EventStateRoot;
import com.rvr.event.planner.es.protobuf.EventObject.EventRecord;
import com.rvr.event.planner.es.protobuf.SnapshotObject.SnapshotRecord;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

public class Converter {
    public static Converter create() {
        return new Converter();
    }

    public EventRecord toEventRecordProtobuf(Event event) {
        EventConverterEnum eventConverter = EventConverterEnum.valueByClassName(event);

        EventRecord.Builder eventRecordBuilder = EventRecord.newBuilder();
        eventRecordBuilder.setAggregateId(event.aggregateId().toString());
        eventRecordBuilder.setEventType(eventConverter.toString());

        Map<String, String> additionalParameters = eventConverter.execute(event);
        additionalParameters.forEach(eventRecordBuilder::putAdditionalParameters);
        return eventRecordBuilder.build();
    }

    public Event toDomain(EventRecord eventRecord) {
        EventConverterEnum eventConverterEnum = EventConverterEnum.
                valueOf(eventRecord.getEventType());
        return eventConverterEnum.convertToDomain(eventRecord);
    }

    public SnapshotRecord toSnapshotRecordProtobuf(EventStateRoot eventStateRoot) {
        SnapshotRecord.Builder snapshotRecordBuilder = SnapshotRecord.newBuilder();
        snapshotRecordBuilder.setAggregateId(eventStateRoot.getAggregateId().toString());
        snapshotRecordBuilder.setVersion(eventStateRoot.getVersion());
        snapshotRecordBuilder.setCounterOfMember(eventStateRoot.getCounterOfMember());
        snapshotRecordBuilder.setState(eventStateRoot.getState().toString());

        if (!StringUtils.isEmpty(eventStateRoot.getMember()))
            snapshotRecordBuilder.setMember(eventStateRoot.getMember());
        if (!StringUtils.isEmpty(eventStateRoot.getPlace()))
            snapshotRecordBuilder.setPlace(eventStateRoot.getPlace().toString());

        return snapshotRecordBuilder.build();
    }

    public EventStateRoot toSnapshotEventState(SnapshotRecord snapshotRecord) {
        EventStateRoot eventStateRoot
                = new EventStateRoot(UUID.fromString(snapshotRecord.getAggregateId()),
                EventAggregator.State.valueOf(snapshotRecord.getState()),
                snapshotRecord.getVersion());
        eventStateRoot.setVersion(snapshotRecord.getVersion());
        eventStateRoot.setCounterOfMember(snapshotRecord.getCounterOfMember());

        if (!StringUtils.isEmpty(snapshotRecord.getMember()))
            eventStateRoot.setMember(snapshotRecord.getMember());
        if (!StringUtils.isEmpty(snapshotRecord.getPlace()))
            eventStateRoot.setPlace(Place.valueOf(snapshotRecord.getPlace()));

        return eventStateRoot;
    }
}
