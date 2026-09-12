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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Three booking strategies live here side by side on purpose, so they can be
 * compared directly (and load-tested directly) against each other:
 * <p>
 * - {@link #book} - the naive Phase 1 version. Plain read-then-write, no
 *   locking. This IS racy: two requests can both read SeatStatus.AVAILABLE
 *   before either writes SeatStatus.BOOKED. The database-level unique
 *   constraint on booking.seat_id is the only thing preventing an actual
 *   double-booking here, and the loser of the race gets an ugly raw 500
 *   instead of a clean 409.
 * <p>
 * - {@link #bookPessimistic} - locks the seat row (SELECT ... FOR UPDATE) for
 *   the duration of the transaction. Concurrent callers queue up and wait
 *   their turn; whoever gets the lock second sees the already-updated status
 *   and is cleanly rejected. Safe, but a queued transaction holds a real
 *   database lock while it waits - under heavy contention on one seat this
 *   serializes those requests.
 * <p>
 * - {@link #bookOptimistic} - no lock at read time. Every concurrent caller
 *   reads the same version; only the first write succeeds because the UPDATE
 *   is scoped to that version, and every other writer's version is now stale,
 *   so their UPDATE affects zero rows and Hibernate raises an
 *   ObjectOptimisticLockingFailureException, which is converted to a clean
 *   409 here. No lock is held while waiting - better throughput under
 *   contention, at the cost of doing (and discarding) more failed work.
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

    public Booking bookPessimistic(Long seatId, Long userId) {
        Seat seat = seatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found: " + seatId));
        User user = userService.getOrThrow(userId);

        // By the time a queued transaction gets here, it's holding the row
        // lock and reading whatever the previous transaction actually
        // committed - never a stale value.
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

    public Booking bookOptimistic(Long seatId, Long userId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found: " + seatId));
        User user = userService.getOrThrow(userId);

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat is not available: " + seatId);
        }

        seat.setStatus(SeatStatus.BOOKED);
        try {
            // saveAndFlush forces the UPDATE (with its "and version = ?"
            // clause) to run right now, inside this try block, instead of at
            // transaction commit - which is what lets us catch the failure
            // here and turn it into a clean 409 rather than a raw 500 later.
            seatRepository.saveAndFlush(seat);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seat was booked by someone else a moment ago: " + seatId);
        }

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
