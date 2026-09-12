package com.seatlock.controller;

import com.seatlock.dto.CreateEventRequest;
import com.seatlock.dto.CreateSeatRequest;
import com.seatlock.dto.EventResponse;
import com.seatlock.dto.SeatResponse;
import com.seatlock.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Events and seats - public, no auth required")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create an event")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        var event = eventService.create(request.name(), request.venue(), request.eventTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }

    @GetMapping
    @Operation(summary = "List all events")
    public List<EventResponse> list() {
        return eventService.findAll().stream().map(EventResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an event by id")
    public EventResponse get(@PathVariable Long id) {
        return EventResponse.from(eventService.getOrThrow(id));
    }

    @PostMapping("/{id}/seats")
    @Operation(summary = "Add a seat to an event")
    public ResponseEntity<SeatResponse> addSeat(@PathVariable Long id, @Valid @RequestBody CreateSeatRequest request) {
        var seat = eventService.addSeat(id, request.seatLabel());
        return ResponseEntity.status(HttpStatus.CREATED).body(SeatResponse.from(seat));
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "List an event's seats")
    public List<SeatResponse> listSeats(@PathVariable Long id) {
        return eventService.findSeats(id).stream().map(SeatResponse::from).toList();
    }
}
