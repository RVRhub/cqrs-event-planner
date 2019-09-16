package com.rvr.event.planner.es.protobuf.converter;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.event.CreateNewEvent;
import com.rvr.event.planner.domain.event.DeclinedEvent;
import com.rvr.event.planner.domain.event.MemberOfferEvent;
import com.rvr.event.planner.domain.event.PlannedEvent;
import com.rvr.event.planner.es.protobuf.EventObject.EventRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public enum EventConverterEnum {

    CREATE(CreateNewEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            CreateNewEvent createNewEvent = (CreateNewEvent) event;
            return Map.of("member", createNewEvent.getMember());
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            return new CreateNewEvent(UUID.fromString(eventRecord.getAggregateId()),
                    eventRecord.getAdditionalParametersOrThrow("member"));
        }
    },
    MEMBER_OFFER(MemberOfferEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            MemberOfferEvent memberOfferEvent = (MemberOfferEvent) event;
            return Map.of("member", memberOfferEvent.getMember(),
                    "place", memberOfferEvent.getPlace().toString());
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            return new MemberOfferEvent(UUID.fromString(eventRecord.getAggregateId()),
                    eventRecord.getAdditionalParametersOrThrow("member"),
                    Place.valueOf(eventRecord.getAdditionalParametersOrThrow("place")));
        }
    },
    PLANNED(PlannedEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            PlannedEvent plannedEvent = (PlannedEvent) event;
            return Map.of("place", plannedEvent.getPlace().toString());
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            return new PlannedEvent(UUID.fromString(eventRecord.getAggregateId()),
                    Place.valueOf(eventRecord.getAdditionalParametersOrThrow("place")));
        }
    },
    DECLINED(DeclinedEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            return Collections.emptyMap();
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            return new DeclinedEvent(UUID.fromString(eventRecord.getAggregateId()));
        }
    };

    private Class clazz;

    public static EventConverterEnum valueByClassName(Event event) {
        for (EventConverterEnum e : values()) {
            if (event.getClass().isAssignableFrom(e.getClazz())) {
                return e;
            }
        }
        return null;
    }

    abstract Map<String, String> execute(final Event event);

    abstract Event convertToDomain(EventRecord eventRecord);
}