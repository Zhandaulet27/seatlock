package com.seatlock.repository;

import com.seatlock.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventId(Long eventId);

    /**
     * Pessimistic-locking read: issues a SELECT ... FOR UPDATE, so the database
     * row is locked for the rest of the current transaction. A second concurrent
     * transaction calling this for the same seat id BLOCKS here until the first
     * transaction commits (or rolls back) - it does not see a stale value, it
     * waits, then reads whatever the first transaction actually left behind.
     * This is what makes BookingService.bookPessimistic() safe under concurrency.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id")
    Optional<Seat> findByIdForUpdate(@Param("id") Long id);
}
