package com.seatlock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "seat",
        // A given event can't have two seats with the same label (e.g. two "A12"s).
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "seat_label"})
)
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "seat_label", nullable = false)
    private String seatLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;

    // Used by Phase 2's optimistic-locking booking strategy. JPA increments this
    // automatically on every update and rejects a stale write with an
    // OptimisticLockException - not wired into any logic yet in Phase 1.
    @Version
    private Long version;
}
