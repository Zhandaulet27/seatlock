package com.seatlock.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank String name,
        @NotBlank String venue,
        @NotNull LocalDateTime eventTime
) {
}
