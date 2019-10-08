package com.rvr.event.planner.es.protobuf;

import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.EventStateRoot;
import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.es.ListEventStream;
import com.rvr.event.planner.es.protobuf.EventObject.EventRecord;
import com.rvr.event.planner.es.protobuf.SnapshotObject.SnapshotRecord;
import com.rvr.event.planner.es.protobuf.converter.Converter;
import com.rvr.event.planner.es.protobuf.file.EsFileManager;
import com.rvr.event.planner.es.protobuf.file.SsFileManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class ProtobufEventStore implements EventStore<Long> {

    public static final String FILE_EVENT_STORE_BIN = "es_";
    public static final String FILE_SNAPSHOT_STORE_BIN = "ss_";

    private final Map<UUID, EsFileManager> eventStreamStorage = new ConcurrentHashMap<>();
    private final Map<UUID, SsFileManager> snapshotStorage = new ConcurrentHashMap<>();

    @Override
    public EventAggregator readEventStateRoot(UUID aggregateIdentifier) {
        Optional<EventStateRoot> snapshotEventState = readSnapshot(aggregateIdentifier);

        if (snapshotEventState.isPresent()) {
            EventStateRoot ssEventState = snapshotEventState.get();
            List<Event> events
                    = loadEventsByGtSequenceVersion(aggregateIdentifier, ssEventState.getVersion());

            var version = ssEventState.getVersion() + events.size();
            return applyEvents(aggregateIdentifier, events, version);
        }

        var eventStream = loadEventStream(aggregateIdentifier);
        long version = eventStream.stream().count();

        return applyEvents(aggregateIdentifier, eventStream.getEvents(), version);
    }

    private EventAggregator applyEvents(UUID aggregateIdentifier, List<Event> events, long version) {
        EventAggregator eventAggregator = new EventAggregator(aggregateIdentifier, version);
        for (Event event : events) {
            eventAggregator = eventAggregator.getEventHandler().apply(event);
        }
        updateSnapshot(eventAggregator.getEventStateAggregate());
        return eventAggregator;
    }

    @Override
    public ListEventStream loadEventStream(UUID aggregateId) {
        log.debug("Load stream from protobuf file by aggregateId: " + aggregateId);
        EsFileManager es = eventStreamStorage.get(aggregateId);
        if (es != null) {
            return es.readEventStream();
        }
        return new ListEventStream();
    }

    @Override
    public void appendEvents(EventStateRoot eventStateAggregate, List<Event> events) {
        if (events != null && events.size() > 0) {
            events.forEach(event -> {
                EventRecord eventRecord = Converter.create().toEventRecordProtobuf(event);
                EsFileManager esFileManager = getESManager(UUID.fromString(eventRecord.getAggregateId()));
                esFileManager.appendRecord(eventRecord, true);
            });
        } else {
            // Command generated no events Saga
        }
    }

    @Override
    public void updateSnapshot(EventStateRoot eventStateAggregate) {
        SnapshotRecord snapshotRecord = Converter.create().toSnapshotRecordProtobuf(eventStateAggregate);
        SsFileManager ssFileManager = getSSManager(UUID.fromString(snapshotRecord.getAggregateId()));
        ssFileManager.appendSnapshot(snapshotRecord, true);
    }

    @Override
    public Optional<EventStateRoot> readSnapshot(UUID aggregateId) {
        log.debug("Load snapshot by aggregateId: " + aggregateId);
        SsFileManager ssFileManager = snapshotStorage.get(aggregateId);
        if (ssFileManager != null) {
            return Optional.ofNullable(ssFileManager.readLastSnapshot());
        }
        return Optional.empty();
    }

    private List<Event> loadEventsByGtSequenceVersion(UUID aggregateId, long version) {
        return loadEventStream(aggregateId).stream()
                .filter(event -> event.getSequenceNumber() >= version)
                .collect(Collectors.toList());
    }

    private EsFileManager getESManager(UUID aggregateId) {
        var esFileManager = eventStreamStorage.get(aggregateId);
        if (esFileManager == null) {
            String fileName = FILE_EVENT_STORE_BIN + aggregateId.toString() + ".bin";
            esFileManager = new EsFileManager(fileName);
            eventStreamStorage.put(aggregateId, esFileManager);
        }
        return esFileManager;
    }

    private SsFileManager getSSManager(UUID aggregateId) {
        var ssFileManager = snapshotStorage.get(aggregateId);
        if (ssFileManager == null) {
            String fileName = FILE_SNAPSHOT_STORE_BIN + aggregateId.toString() + ".bin";
            ssFileManager = new SsFileManager(fileName);
            snapshotStorage.put(aggregateId, ssFileManager);
        }
        return ssFileManager;
    }
}
