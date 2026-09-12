package com.seatlock.controller;

import com.seatlock.dto.AuthResponse;
import com.seatlock.dto.LoginRequest;
import com.seatlock.dto.RegisterRequest;
import com.seatlock.dto.UserResponse;
import com.seatlock.security.AppUserPrincipal;
import com.seatlock.security.JwtService;
import com.seatlock.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration and login - no token required")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Password is BCrypt-hashed before storage.")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        var user = userService.create(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in", description = "Returns a JWT - paste it into the Authorize button above to call the secured endpoints.")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticate(request);
        var principal = (AppUserPrincipal) authentication.getPrincipal();
        return new AuthResponse(jwtService.generateToken(principal));
    }

    private org.springframework.security.core.Authentication authenticate(LoginRequest request) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            // Deliberately vague - "invalid email or password" rather than
            // distinguishing "no such user" from "wrong password", which
            // would let an attacker enumerate registered emails.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }
}
