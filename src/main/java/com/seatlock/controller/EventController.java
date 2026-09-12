package com.seatlock.controller;

import com.seatlock.dto.CreateEventRequest;
import com.seatlock.dto.CreateSeatRequest;
import com.seatlock.dto.EventResponse;
import com.seatlock.dto.SeatResponse;
import com.seatlock.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        var event = eventService.create(request.name(), request.venue(), request.eventTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.from(event));
    }

    @GetMapping
    public List<EventResponse> list() {
        return eventService.findAll().stream().map(EventResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) {
        return EventResponse.from(eventService.getOrThrow(id));
    }

    @PostMapping("/{id}/seats")
    public ResponseEntity<SeatResponse> addSeat(@PathVariable Long id, @Valid @RequestBody CreateSeatRequest request) {
        var seat = eventService.addSeat(id, request.seatLabel());
        return ResponseEntity.status(HttpStatus.CREATED).body(SeatResponse.from(seat));
    }

    @GetMapping("/{id}/seats")
    public List<SeatResponse> listSeats(@PathVariable Long id) {
        return eventService.findSeats(id).stream().map(SeatResponse::from).toList();
    }
}
