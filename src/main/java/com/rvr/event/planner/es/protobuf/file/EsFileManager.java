package com.rvr.event.planner.es.protobuf.file;

import com.rvr.event.planner.es.ListEventStream;
import com.rvr.event.planner.es.protobuf.EventObject;
import com.rvr.event.planner.es.protobuf.EventObject.EventRecord;
import com.rvr.event.planner.es.protobuf.converter.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;

@Slf4j
public class EsFileManager extends FileManager {

    public EsFileManager(String fileName) {
        super(fileName);
    }

    public void appendRecord(EventRecord eventRecord, boolean streaming) {
        append(eventRecord, streaming);
    }

    public ListEventStream readEventStream() {
        try {
            ListEventStream listEventStream = new ListEventStream();
            FileInputStream fileInputStream = new FileInputStream(fileName);
            while (true) {
                EventRecord eventRecord = EventObject.EventRecord.parseDelimitedFrom(fileInputStream);
                if (eventRecord == null) break;
                listEventStream = listEventStream
                        .append(Collections.singletonList(Converter.create().toDomain(eventRecord)));
            }
//            EventRecord eventRecord = EventRecord.newBuilder()
//                    .mergeFrom(new FileInputStream(fileName))
//                    .build();
//            List<Event> events = Collections.singletonList(Converter.create().toDomain(eventRecord));
//            return listEventStream.append(events);
            return listEventStream;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
