package com.rvr.event.planner;

import com.rvr.event.planner.domain.Event;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class EventStringUtil {
    public static String toString(Event event) {
        String simpleName = event.getClass().getSimpleName();
        Map<String, Object> values = new HashMap<String, Object>();
        for (Field field : event.getClass().getFields()) {
            try {
                Object object = field.get(event);
                if (object instanceof UUID) {
                    object = ((UUID)object).getLeastSignificantBits() % 255;
                }
                values.put(field.getName(), object);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return simpleName + values;
    }
}