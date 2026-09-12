package com.seatlock.controller;

import com.seatlock.dto.UserResponse;
import com.seatlock.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// User creation now happens through POST /api/v1/auth/register (Phase 3),
// which hashes the password properly - this controller is just for lookups.
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Requires a Bearer token - see /api/v1/auth/login")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.getOrThrow(id));
    }
}
