package com.seatlock.dto;

import jakarta.validation.constraints.NotNull;

// userId used to be passed explicitly here (Phase 1/2). Now that Phase 3 adds
// auth, the caller's identity comes from their JWT instead - see
// BookingController.currentUserId(Authentication).
public record CreateBookingRequest(
        @NotNull Long seatId
) {
}
