package com.rvr.event.planner.web;

import com.rvr.event.planner.domain.Place;
import com.rvr.event.planner.domain.command.CreateEventCommand;
import com.rvr.event.planner.domain.command.MakeDecisionCommand;
import com.rvr.event.planner.domain.command.MemberOfferCommand;
import com.rvr.event.planner.domain.processors.EventState;
import com.rvr.event.planner.domain.processors.EventsProjection;
import com.rvr.event.planner.service.CommandHandlerService;
import com.rvr.event.planner.service.QueryCurrentStatusService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class EventControllerV1 {
    private CommandHandlerService commandHandlerService;
    private QueryCurrentStatusService queryCurrentStatusService;
    private EventsProjection eventsProjection;

    @PostMapping
    public ResponseEntity<String> createEvent(@RequestParam("memberEmail") String email) throws Exception {
        UUID eventId = UUID.randomUUID();
        commandHandlerService.handle(new CreateEventCommand(eventId, email));
        return new ResponseEntity<>(eventId.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/{eventId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EventDTO getEventByEventId(
            @PathVariable("eventId") String eventId) throws Exception {
        EventState event = queryCurrentStatusService
                .getEventStateByEventId(UUID.fromString(eventId), false);

        return new EventDTO(event.getEventId().toString(),
                event.getCreatedBy(),
                event.getPlace(),
                event.getState().toString(),
                event.getOffers());
    }

    @PostMapping("/{eventId}")
    public void putOffer(
            @PathVariable("eventId") String eventId,
            @RequestParam("memberEmail") String email,
            @RequestParam("place") Place place) throws Exception {

        commandHandlerService.handle(new MemberOfferCommand(UUID.fromString(eventId), email, place));
    }

    @PatchMapping("/{eventId}")
    public void makeDecision(
            @PathVariable("eventId") String eventId) throws Exception {

        commandHandlerService.handle(new MakeDecisionCommand(UUID.fromString(eventId)));
    }
}