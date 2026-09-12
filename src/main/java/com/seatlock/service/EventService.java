package com.seatlock.service;

import com.seatlock.entity.Event;
import com.seatlock.entity.Seat;
import com.seatlock.repository.EventRepository;
import com.seatlock.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public Event create(String name, String venue, LocalDateTime eventTime) {
        Event event = new Event();
        event.setName(name);
        event.setVenue(venue);
        event.setEventTime(eventTime);
        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event getOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found: " + id));
    }

    public Seat addSeat(Long eventId, String seatLabel) {
        Event event = getOrThrow(eventId);
        Seat seat = new Seat();
        seat.setEvent(event);
        seat.setSeatLabel(seatLabel);
        return seatRepository.save(seat);
    }

    @Transactional(readOnly = true)
    public List<Seat> findSeats(Long eventId) {
        getOrThrow(eventId); // 404s cleanly if the event itself doesn't exist
        return seatRepository.findByEventId(eventId);
    }
}
