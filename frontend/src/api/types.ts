// Mirrors the backend's DTOs 1:1 (see backend/src/main/java/com/seatlock/dto)
// so a shape mismatch is a compile error here, not a runtime surprise.

export interface UserResponse {
  id: number
  email: string
}

export interface AuthResponse {
  token: string
}

export interface EventResponse {
  id: number
  name: string
  venue: string
  eventTime: string // ISO-8601 local date-time, e.g. "2026-12-31T19:00:00"
}

export type SeatStatus = 'AVAILABLE' | 'BOOKED'

export interface SeatResponse {
  id: number
  eventId: number
  seatLabel: string
  status: SeatStatus
}

export interface BookingResponse {
  id: number
  seatId: number
  seatLabel: string
  userId: number
  status: string
  bookedAt: string // ISO-8601 instant
}

export interface RegisterRequest {
  email: string
  password: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface CreateEventRequest {
  name: string
  venue: string
  eventTime: string
}

export interface CreateSeatRequest {
  seatLabel: string
}

export interface CreateBookingRequest {
  seatId: number
}

// The three booking strategies BookingController exposes side by side -
// the actual point of this project. See EventDetailPage for the write-up
// shown next to the picker.
export type BookingStrategy = 'naive' | 'pessimistic' | 'optimistic'
