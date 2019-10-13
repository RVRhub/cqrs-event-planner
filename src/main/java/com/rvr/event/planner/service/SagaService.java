package com.rvr.event.planner.service;

import com.google.common.collect.Lists;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.event.AcceptEvent;
import com.rvr.event.planner.domain.event.PlannedEvent;
import com.rvr.event.planner.domain.event.RejectEvent;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.saga.PlannedEventSaga;
import com.rvr.event.planner.es.SagaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SEC - Saga Execution Coordinator
 */
@Slf4j
@Service
public class SagaService {
    private final SagaRepository sagaRepository;

    @Autowired
    public SagaService(SagaRepository sagaRepository) {
        this.sagaRepository = sagaRepository;
    }

    public void startSaga(UUID sagaId, List<Event> plannedEvent) {
        PlannedEventSaga plannedEventSaga = PlannedEventSaga.builder()
                .key(sagaId.toString())
                .handler("reviewPlannedEventService")
                .requiredEvents(
                        List.of(PlannedEvent.class, AcceptEvent.class)
                )
                .rejectionEvent(RejectEvent.class)
                .build();

        plannedEventSaga.getEvents().addAll(plannedEvent);
        sagaRepository.addNewSaga(sagaId.toString(), plannedEventSaga);
    }

    public List<Event> apply(UUID sagaId, List<Event> event, EventAggregator eventAggregator) {
        PlannedEventSaga saga = sagaRepository.getSaga(sagaId.toString()).get();

        List<Event> requiredEvents = event.stream()
                .filter(t -> isRequiredEventClass(saga, t))
                .collect(Collectors.toList());

        List<Event> rejectionEvents = event.stream()
                .filter(t -> t.getClass().isAssignableFrom(saga.getRejectionEvent()))
                .collect(Collectors.toList());

        requiredEvents.forEach(t -> sagaRepository.appendEvent(sagaId.toString(), t));
        rejectionEvents.forEach(t -> sagaRepository.appendEvent(sagaId.toString(), t));

        List<Event> events = Lists.newArrayList();
        if (allEventsOccurred(saga)) {
            events.addAll(eventAggregator.resolve(saga));
            saga.setDone(true);
        }
        if (events.isEmpty() && isErrorEvent(saga, event)) {
            // rollback process
            events.addAll(eventAggregator.reject(saga));
        }
        return events;
    }

    private boolean isRequiredEventClass(PlannedEventSaga saga, Event t) {
        return saga.getRequiredEvents()
                .stream()
                .anyMatch(requiredEventClass -> t.getClass().isAssignableFrom(requiredEventClass));
    }

    public boolean isExistSaga(UUID aggregateId) {
        return sagaRepository.getSaga(aggregateId.toString())
                .isPresent();
    }

    public boolean isEndSaga(UUID aggregateId) {
        return sagaRepository.getSaga(aggregateId.toString())
                .map(PlannedEventSaga::isDone)
                .orElse(false);
    }

    private boolean isErrorEvent(PlannedEventSaga saga, Object event) {
        return false;
    }

    private boolean allEventsOccurred(PlannedEventSaga saga) {
        return saga.getEvents().stream()
                .filter(t -> isRequiredEventClass(saga, t))
                .count() > 2;
    }

}
