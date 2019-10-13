package com.rvr.event.planner.es;


import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.processors.saga.PlannedEventSaga;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SagaRepository {
    private Map<String, PlannedEventSaga> sagaStore = new ConcurrentHashMap<>();

    public Optional<PlannedEventSaga> getSaga(String sagaId) {
        return Optional.ofNullable(sagaStore.get(sagaId));
    }

    public void appendEvent(String sagaId, Event event) {
        PlannedEventSaga plannedEventSaga = sagaStore.get(sagaId);
        if (plannedEventSaga == null) {
            throw new IllegalArgumentException(String
                    .format("Saga with ID %s doesn't exists.", sagaId));
        }
        plannedEventSaga.getEvents().add(event);
    }

    public void addNewSaga(String sagaId, PlannedEventSaga plannedEventSaga) {
        sagaStore.put(sagaId, plannedEventSaga);
    }
}
