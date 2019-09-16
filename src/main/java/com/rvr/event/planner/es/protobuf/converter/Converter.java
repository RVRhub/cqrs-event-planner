package com.rvr.event.planner.es.protobuf.converter;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.es.protobuf.EventObject.EventRecord;

import java.util.Map;

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
}
