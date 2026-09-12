package com.seatlock;

import com.seatlock.entity.Event;
import com.seatlock.entity.Seat;
import com.seatlock.entity.User;
import com.seatlock.repository.EventRepository;
import com.seatlock.repository.SeatRepository;
import com.seatlock.repository.UserRepository;
import com.seatlock.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The actual proof-of-concept for this whole project: fire many booking
 * requests at the SAME seat at the SAME instant and confirm exactly one wins.
 * <p>
 * This is a real integration test - it runs against your actual local
 * PostgreSQL database (whatever application.yml points to), the same one the
 * app uses when you run it normally. That's a deliberate simplification for
 * now: it's not isolated or CI-ready (no Testcontainers / dedicated test
 * database yet - a Phase 6+ improvement worth mentioning if asked about test
 * strategy), but it's genuinely exercising real Postgres row locks and real
 * optimistic-locking version checks, which an in-memory database would not
 * reliably reproduce the same way.
 * <p>
 * Requires: your local Postgres running and the "seatlock" database reachable,
 * exactly like running the app normally.
 */
@SpringBootTest
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void onlyOneBookingSucceeds_pessimistic() throws Exception {
        Seat seat = createTestSeat("pess");
        runConcurrentBookings(seat.getId(), true, 20);
    }

    @Test
    void onlyOneBookingSucceeds_optimistic() throws Exception {
        Seat seat = createTestSeat("opt");
        runConcurrentBookings(seat.getId(), false, 20);
    }

    private Seat createTestSeat(String label) {
        Event event = new Event();
        event.setName("Concurrency Test Event");
        event.setVenue("Test Venue");
        event.setEventTime(LocalDateTime.now().plusDays(1));
        event = eventRepository.save(event);

        Seat seat = new Seat();
        seat.setEvent(event);
        // unique per run so repeated test executions don't collide
        seat.setSeatLabel(label + "-" + System.nanoTime());
        return seatRepository.save(seat);
    }

    /**
     * Fires {@code threadCount} threads at the same seat simultaneously
     * (held back on a latch so they all release at once, rather than
     * trickling in one at a time) and asserts exactly one booking succeeds
     * and every other caller gets a clean conflict - never a crash, never a
     * silent double-booking.
     */
    private void runConcurrentBookings(Long seatId, boolean pessimistic, int threadCount) throws Exception {
        Long[] userIds = new Long[threadCount];
        for (int i = 0; i < threadCount; i++) {
            User user = new User();
            user.setEmail("conc-test-" + System.nanoTime() + "-" + i + "@example.com");
            userIds[i] = userRepository.save(user).getId();
        }

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (Long userId : userIds) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    if (pessimistic) {
                        bookingService.bookPessimistic(seatId, userId);
                    } else {
                        bookingService.bookOptimistic(seatId, userId);
                    }
                    successCount.incrementAndGet();
                } catch (ResponseStatusException e) {
                    // Expected outcome for every loser of the race - not a bug.
                    conflictCount.incrementAndGet();
                }
            }));
        }

        ready.await(); // wait until every thread is actually running and blocked on start.await()
        start.countDown(); // release them all at the same instant

        // future.get() rethrows anything unexpected (a real bug) as a test
        // failure with a full stack trace, instead of it silently vanishing
        // inside a background thread.
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(1, successCount.get(), "Exactly one booking should succeed");
        assertEquals(threadCount - 1, conflictCount.get(), "Every other request should be cleanly rejected");
    }
}
