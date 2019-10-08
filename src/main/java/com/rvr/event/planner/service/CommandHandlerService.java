package com.rvr.event.planner.service;

import com.rvr.event.planner.domain.Command;
import com.rvr.event.planner.domain.Event;
import com.rvr.event.planner.domain.processors.EventAggregator;
import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.service.utils.CommandHandlerLookup;
import com.rvr.event.planner.service.utils.ReflectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CommandHandlerService {
    private final EventStore eventStore;
    private CommandHandlerLookup commandHandlerLookup;

    @Autowired
    public CommandHandlerService(EventStore eventStore) {
        this.eventStore = eventStore;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, EventAggregator.class);
    }

    public void handle(Command command) throws Exception {
        var eventAggregator = eventStore.readEventStateRoot(command.aggregateId());

        List<Event> events = eventAggregator.getCommandHandler().apply(command);

        var eventStateAggregate = eventAggregator.getEventStateAggregate();

        eventStore.appendEvents(eventStateAggregate, events);
    }

    @Deprecated
    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException {
        return commandHandlerLookup.targetType(command).newInstance();
    }
}