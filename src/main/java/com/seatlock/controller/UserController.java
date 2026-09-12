package com.seatlock.controller;

import com.seatlock.dto.UserResponse;
import com.seatlock.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// User creation now happens through POST /api/v1/auth/register (Phase 3),
// which hashes the password properly - this controller is just for lookups.
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.getOrThrow(id));
    }
}
