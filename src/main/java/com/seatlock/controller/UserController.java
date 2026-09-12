package com.seatlock.controller;

import com.seatlock.dto.CreateUserRequest;
import com.seatlock.dto.UserResponse;
import com.seatlock.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Placeholder for real registration - Phase 3 replaces this with
// Spring Security + JWT (POST /api/v1/auth/register, /api/v1/auth/login).
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        var user = userService.create(request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return UserResponse.from(userService.getOrThrow(id));
    }
}
