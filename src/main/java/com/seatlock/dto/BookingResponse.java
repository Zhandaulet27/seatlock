package com.seatlock.dto;

import com.seatlock.entity.Booking;

import java.time.Instant;

public record BookingResponse(
        Long id,
        Long seatId,
        String seatLabel,
        Long userId,
        String status,
        Instant bookedAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getSeat().getId(),
                booking.getSeat().getSeatLabel(),
                booking.getUser().getId(),
                booking.getStatus().name(),
                booking.getBookedAt()
        );
    }
}
