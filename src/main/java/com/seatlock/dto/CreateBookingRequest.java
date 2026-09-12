package com.seatlock.dto;

import jakarta.validation.constraints.NotNull;

// userId is passed explicitly for now because there's no authentication yet
// (Phase 1). Phase 3 replaces this with the logged-in user taken from the JWT,
// and this field goes away.
public record CreateBookingRequest(
        @NotNull Long seatId,
        @NotNull Long userId
) {
}
