package com.seatlock.controller;

import com.seatlock.dto.BookingResponse;
import com.seatlock.dto.CreateBookingRequest;
import com.seatlock.security.AppUserPrincipal;
import com.seatlock.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bookings", description = "Requires a Bearer token - see /api/v1/auth/login")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    // Naive Phase 1 version - kept around specifically so it can be load
    // tested side by side with the two strategies below and show the
    // difference concurrency control actually makes.
    @PostMapping
    @Operation(summary = "Book a seat - naive strategy",
            description = "No locking. Racy under concurrent requests; kept only for side-by-side load testing.")
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody CreateBookingRequest request,
                                                 Authentication authentication) {
        var booking = bookingService.book(request.seatId(), currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @PostMapping("/pessimistic")
    @Operation(summary = "Book a seat - pessimistic locking",
            description = "Locks the seat row (SELECT ... FOR UPDATE) for the duration of the transaction.")
    public ResponseEntity<BookingResponse> bookPessimistic(@Valid @RequestBody CreateBookingRequest request,
                                                             Authentication authentication) {
        var booking = bookingService.bookPessimistic(request.seatId(), currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @PostMapping("/optimistic")
    @Operation(summary = "Book a seat - optimistic locking",
            description = "No lock held while waiting; relies on the seat's @Version column to reject stale writes with a 409.")
    public ResponseEntity<BookingResponse> bookOptimistic(@Valid @RequestBody CreateBookingRequest request,
                                                            Authentication authentication) {
        var booking = bookingService.bookOptimistic(request.seatId(), currentUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @GetMapping("/me")
    @Operation(summary = "List the authenticated user's bookings")
    public List<BookingResponse> listMine(Authentication authentication) {
        return bookingService.findByUser(currentUserId(authentication)).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a booking", description = "Only the booking's own owner may cancel it (403 otherwise).")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication authentication) {
        bookingService.cancel(id, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        return ((AppUserPrincipal) authentication.getPrincipal()).getId();
    }
}
