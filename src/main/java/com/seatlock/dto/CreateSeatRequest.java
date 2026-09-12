package com.seatlock.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSeatRequest(
        @NotBlank String seatLabel
) {
}
