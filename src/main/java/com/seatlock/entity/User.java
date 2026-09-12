package com.seatlock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    // Null for now - Phase 3 (auth) is what actually starts setting this via
    // registration + password hashing. Don't build login logic against this yet.
    private String passwordHash;

    @Column(nullable = false)
    private String role = "USER";

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
