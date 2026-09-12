package com.seatlock.dto;

import com.seatlock.entity.Seat;
import com.seatlock.entity.SeatStatus;

public record SeatResponse(Long id, Long eventId, String seatLabel, SeatStatus status) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.getId(), seat.getEvent().getId(), seat.getSeatLabel(), seat.getStatus());
    }
}
