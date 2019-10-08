package com.rvr.event.planner.es.protobuf.file;

import com.google.protobuf.MessageLite;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public abstract class FileManager<T extends MessageLite> {
    private static final String STORAGE_FOLDER = "storage/";

    protected final String fileName;

    protected FileManager(String fileName) {
        this.fileName = STORAGE_FOLDER + fileName;
    }

    public void append(T message, boolean streaming) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName, true);

            if (streaming) {
                message.writeDelimitedTo(fos);
            } else {
                message.writeTo(fos);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new IllegalStateException("Unexpected I/O error while writing to data buffer", ex);
        }
    }
}
