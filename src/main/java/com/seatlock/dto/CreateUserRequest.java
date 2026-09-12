package com.seatlock.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Temporary stand-in for real registration until Phase 3 adds
// Spring Security + JWT and password hashing.
public record CreateUserRequest(
        @NotBlank @Email String email
) {
}
