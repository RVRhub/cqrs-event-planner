package com.rvr.event.planner.service;

import com.rvr.event.planner.domain.Command;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.event.AcceptEvent;
import com.rvr.event.planner.domain.event.PlannedEvent;
import com.rvr.event.planner.domain.event.RejectEvent;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.domain.processors.handlers.CommandHandler;
import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.service.utils.CommandHandlerLookup;
import com.rvr.event.planner.service.utils.ReflectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class CommandHandlerService {
    private final EventStore eventStore;
    private final SagaService sagaService;
    private CommandHandlerLookup commandHandlerLookup;


    @Autowired
    public CommandHandlerService(EventStore eventStore, SagaService sagaService) {
        this.eventStore = eventStore;
        this.sagaService = sagaService;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, EventAggregator.class);
    }

    public void handle(Command command) throws Exception {
        var eventAggregator = eventStore.readEventStateRoot(command.aggregateId());

        List<Event> events = applyCommand(command, eventAggregator);

        var eventStateAggregate = eventAggregator.getEventStateAggregate();

        eventStore.appendEvents(eventStateAggregate, events);
    }

    @Deprecated
    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException {
        return commandHandlerLookup.targetType(command).newInstance();
    }

    private List<Event> applyCommand(Command command, EventAggregator eventAggregator) {
        CommandHandler commandHandler = eventAggregator.getCommandHandler();

        List<Event> events = commandHandler.apply(command);

        if (isEventExist(events, PlannedEvent.class)) {
            sagaService.startSaga(command.aggregateId(), events);
        } else if (isEventExist(events, AcceptEvent.class)
                || isEventExist(events, RejectEvent.class)) {

            List<Event> sagaResult = sagaService
                    .apply(command.aggregateId(), events, eventAggregator);
            if (sagaService.isEndSaga(command.aggregateId())) {
                events = sagaResult;
            } else {
                events = new ArrayList<>();
            }
        }

        return events;
    }

    private boolean isEventExist(List<Event> events, Class clazz) {
        return events.stream()
                .anyMatch(t -> t.getClass().isAssignableFrom(clazz));
    }
}