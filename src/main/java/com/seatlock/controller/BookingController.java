package com.seatlock.controller;

import com.seatlock.dto.BookingResponse;
import com.seatlock.dto.CreateBookingRequest;
import com.seatlock.security.AppUserPrincipal;
import com.seatlock.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody CreateBookingRequest request,
                                                 Authentication authentication) {
        var booking = bookingService.book(request.seatId(), currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @PostMapping("/pessimistic")
    public ResponseEntity<BookingResponse> bookPessimistic(@Valid @RequestBody CreateBookingRequest request,
                                                             Authentication authentication) {
        var booking = bookingService.bookPessimistic(request.seatId(), currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @PostMapping("/optimistic")
    public ResponseEntity<BookingResponse> bookOptimistic(@Valid @RequestBody CreateBookingRequest request,
                                                            Authentication authentication) {
        var booking = bookingService.bookOptimistic(request.seatId(), currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @GetMapping("/me")
    public List<BookingResponse> listMine(Authentication authentication) {
        return bookingService.findByUser(currentUserId(authentication)).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication authentication) {
        bookingService.cancel(id, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        return ((AppUserPrincipal) authentication.getPrincipal()).getId();
    }
}
