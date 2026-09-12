package com.seatlock.service;

import com.seatlock.entity.Booking;
import com.seatlock.entity.BookingStatus;
import com.seatlock.entity.Seat;
import com.seatlock.entity.SeatStatus;
import com.seatlock.entity.User;
import com.seatlock.repository.BookingRepository;
import com.seatlock.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Phase 1 version: plain read-then-write, no locking strategy yet.
 * This IS racy under real concurrent load - two requests can both read
 * SeatStatus.AVAILABLE before either writes SeatStatus.BOOKED, and the
 * database-level unique constraint on booking.seat_id is what actually
 * saves you (one of the two inserts will fail).
 * <p>
 * Phase 2 replaces the "read then write" logic here with an explicit
 * pessimistic-locking version and an optimistic-locking version, plus a
 * concurrency test proving only one request wins.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final UserService userService;

    public Booking book(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found: " + seatId));
        User user = userService.getOrThrow(userId);

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is not available: " + seatId);
        }

        seat.setStatus(SeatStatus.BOOKED);
        seatRepository.save(seat);

        Booking booking = new Booking();
        booking.setSeat(seat);
        booking.setUser(user);
        return bookingRepository.save(booking);
    }

    @Transactional(readOnly = true)
    public List<Booking> findByUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public void cancel(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found: " + bookingId));

        booking.setStatus(BookingStatus.CANCELLED);
        Seat seat = booking.getSeat();
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);
        bookingRepository.delete(booking);
    }
}
