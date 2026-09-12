package com.seatlock.controller;

import com.seatlock.dto.BookingResponse;
import com.seatlock.dto.CreateBookingRequest;
import com.seatlock.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // Naive Phase 1 version - kept around specifically so it can be load
    // tested side by side with the two strategies below and show the
    // difference concurrency control actually makes.
    @PostMapping
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody CreateBookingRequest request) {
        var booking = bookingService.book(request.seatId(), request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @PostMapping("/pessimistic")
    public ResponseEntity<BookingResponse> bookPessimistic(@Valid @RequestBody CreateBookingRequest request) {
        var booking = bookingService.bookPessimistic(request.seatId(), request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @PostMapping("/optimistic")
    public ResponseEntity<BookingResponse> bookOptimistic(@Valid @RequestBody CreateBookingRequest request) {
        var booking = bookingService.bookOptimistic(request.seatId(), request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    // Temporary substitute for GET /bookings/me until Phase 3 adds auth and
    // the user comes from the JWT instead of a path variable.
    @GetMapping("/user/{userId}")
    public List<BookingResponse> listForUser(@PathVariable Long userId) {
        return bookingService.findByUser(userId).stream().map(BookingResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        bookingService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
