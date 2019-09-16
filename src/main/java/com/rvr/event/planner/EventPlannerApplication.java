package com.rvr.event.planner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EventPlannerApplication {

    // add rebbit mq
    // add how to implement and support snapshoting
    public static void main(String[] args) {
        SpringApplication.run(EventPlannerApplication.class, args);
    }

}