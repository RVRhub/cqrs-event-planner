package com.rvr.event.planner.es.protobuf;

import com.google.common.collect.Lists;
import com.google.protobuf.Message;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.es.ListEventStream;
import com.rvr.event.planner.es.protobuf.EventObject.EventRecord;
import com.rvr.event.planner.es.protobuf.converter.Converter;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ProtobufEventStore implements EventStore<Long> {
    public static final String FILE_EVENT_STORE_BIN = "eventStore.bin";
    private final Map<UUID, ListEventStream> streams = new ConcurrentHashMap<>();

    private DataOutputStream dataOutputStream;
    private FileOutputStream outputStream;

    public ProtobufEventStore() throws Exception{
        outputStream = new FileOutputStream(FILE_EVENT_STORE_BIN);
        dataOutputStream = new DataOutputStream(outputStream);
    }

    @Override
    public ListEventStream loadEventStream(UUID aggregateId)  {
        List<EventRecord> eventRecords = readEventsStoreFromBinaryFile();
        log.info(eventRecords.toString());
        return null;
    }

    @Override
    public void store(UUID aggregateId, long version, List<Event> events) {
        events.forEach(event -> {
            EventRecord eventRecord = Converter.create().toEventRecordProtobuf(event);
            appendRecord(eventRecord, true);
        });
    }

    @Override
    public Observable<Event> all() {
        return null;
    }

    private void closeOutputStream() {
        try {
            dataOutputStream.close();
            outputStream.close();
        } catch (Exception e) {
            log.error("AAAA", e);
        }
    }

    private void writeEventsToBinaryFile(EventRecord eventsStore) {
        try {
            FileOutputStream outputStream = new FileOutputStream(FILE_EVENT_STORE_BIN);
            eventsStore.writeTo(outputStream);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<EventRecord> readEventsStoreFromBinaryFile() {
        try {
            FileInputStream input = new FileInputStream(FILE_EVENT_STORE_BIN);
            List<EventRecord> eventRecords = Lists.newArrayList();
            while (true)
            {
                EventRecord eventRecord = EventRecord.parseDelimitedFrom(input);
                if(eventRecord == null) break;
                eventRecords.add(eventRecord);
            }
            return eventRecords;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private void appendRecord(Message message,  boolean streaming) {
       // DataBuffer buffer = new DefaultDataBufferFactory().allocateBuffer();
       // OutputStream outputStream = buffer.asOutputStream();
        try {
            if (streaming) {
                message.writeDelimitedTo(dataOutputStream);
            }
            else {
                message.writeTo(dataOutputStream);
            }
            dataOutputStream.flush();
        }
        catch (IOException ex) {
            throw new IllegalStateException("Unexpected I/O error while writing to data buffer", ex);
        }
    }
}
