package com.rvr.event.planner.es.protobuf.converter;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.event.*;
import com.rvr.event.planner.es.protobuf.EventObject.EventRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.tomcat.util.buf.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public enum EventConverterEnum {

    CREATE(CreateNewEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            CreateNewEvent createNewEvent = (CreateNewEvent) event;
            return Map.of("member", createNewEvent.getMember(),
                    "sequenceNumber", String.valueOf(createNewEvent.getSequenceNumber()));
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            long sequenceNumber = Long.parseLong(eventRecord.getAdditionalParametersOrThrow("sequenceNumber"));
            return new CreateNewEvent(UUID.fromString(eventRecord.getAggregateId()),
                    eventRecord.getAdditionalParametersOrThrow("member"),
                    sequenceNumber);
        }
    },
    MEMBER_OFFER(MemberOfferEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            MemberOfferEvent memberOfferEvent = (MemberOfferEvent) event;
            return Map.of("member", memberOfferEvent.getMember(),
                    "place", memberOfferEvent.getPlace().toString(),
                    "sequenceNumber", String.valueOf(memberOfferEvent.getSequenceNumber()));
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            long sequenceNumber = Long.parseLong(eventRecord.getAdditionalParametersOrThrow("sequenceNumber"));
            return new MemberOfferEvent(UUID.fromString(eventRecord.getAggregateId()),
                    eventRecord.getAdditionalParametersOrThrow("member"),
                    Place.valueOf(eventRecord.getAdditionalParametersOrThrow("place")),
                    sequenceNumber);
        }
    },
    ADD_PARTICIPANTS(AddParticipantsEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            AddParticipantsEvent addParticipantsEvent = (AddParticipantsEvent) event;
            return Map.of("members", StringUtils.join(addParticipantsEvent.getMembers(), ','),
                    "sequenceNumber", String.valueOf(addParticipantsEvent.getSequenceNumber()));
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            long sequenceNumber = Long.parseLong(eventRecord.getAdditionalParametersOrThrow("sequenceNumber"));
            return new AddParticipantsEvent(UUID.fromString(eventRecord.getAggregateId()),
                    Arrays.asList(eventRecord.getAdditionalParametersOrThrow("members").split(",")),
                    sequenceNumber);
        }
    },
    PLANNED(PlannedEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            PlannedEvent plannedEvent = (PlannedEvent) event;
            return Map.of("place", plannedEvent.getPlace().toString(),
                    "sequenceNumber", String.valueOf(plannedEvent.getSequenceNumber()));
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            long sequenceNumber = Long.parseLong(eventRecord.getAdditionalParametersOrThrow("sequenceNumber"));
            return new PlannedEvent(UUID.fromString(eventRecord.getAggregateId()),
                    Place.valueOf(eventRecord.getAdditionalParametersOrThrow("place")),
                    sequenceNumber);
        }
    },
    ORGANIZED_EVENT(OrganizedEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            OrganizedEvent organizedEvent = (OrganizedEvent) event;
            return Map.of("members", StringUtils.join(organizedEvent.getMembers(), ','),
                    "sequenceNumber", String.valueOf(organizedEvent.getSequenceNumber()));
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            long sequenceNumber = Long.parseLong(eventRecord.getAdditionalParametersOrThrow("sequenceNumber"));
            return new OrganizedEvent(UUID.fromString(eventRecord.getAggregateId()),
                    Arrays.asList(eventRecord.getAdditionalParametersOrThrow("members").split(",")),
                    sequenceNumber);
        }
    },
    DECLINED(DeclinedEvent.class) {
        @Override
        Map<String, String> execute(final Event event) {
            return Map.of("sequenceNumber", String.valueOf(event.getSequenceNumber()));
        }

        @Override
        Event convertToDomain(EventRecord eventRecord) {
            long sequenceNumber = Long.parseLong(eventRecord.getAdditionalParametersOrThrow("sequenceNumber"));
            return new DeclinedEvent(UUID.fromString(eventRecord.getAggregateId()), sequenceNumber);
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
