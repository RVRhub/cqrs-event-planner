package com.rvr.event.planner.es.protobuf.file;

import com.rvr.event.planner.domain.processors.EventStateRoot;
import com.rvr.event.planner.es.protobuf.SnapshotObject.SnapshotRecord;
import com.rvr.event.planner.es.protobuf.converter.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
public class SsFileManager extends FileManager {

    public SsFileManager(String fileName) {
        super(fileName);
    }

    public void appendSnapshot(SnapshotRecord snapshotRecord, boolean streaming) {
        append(snapshotRecord, streaming);
    }

    public EventStateRoot readLastSnapshot() {
        EventStateRoot eventStateRoot = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            while (true) {
                SnapshotRecord snapshotRecord = SnapshotRecord.parseDelimitedFrom(fileInputStream);
                if (snapshotRecord == null) break;
                eventStateRoot = Converter.create().toSnapshotEventState(snapshotRecord);
            }
            return eventStateRoot;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return eventStateRoot;
    }
}
