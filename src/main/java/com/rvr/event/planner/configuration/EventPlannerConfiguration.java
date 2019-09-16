package com.rvr.event.planner.configuration;

import com.rvr.event.planner.es.EventStore;
import com.rvr.event.planner.es.memory.InMemoryEventStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventPlannerConfiguration {

    @Bean
    public EventStore eventStore() {
        return new InMemoryEventStore();
    }
}
