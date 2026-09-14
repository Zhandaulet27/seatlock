import { useEffect, useState } from 'react'
import { ApiError, apiFetch } from '../api/client'
import type { BookingResponse } from '../api/types'

// GET /api/v1/bookings/me and DELETE /api/v1/bookings/{id} both require a
// Bearer token - this page only renders inside <ProtectedRoute>.
//
// Note: BookingService.cancel() deletes the row entirely rather than just
// flipping its status, so a cancelled booking simply disappears from this
// list rather than showing up with a "CANCELLED" badge.
export function MyBookingsPage() {
  const [bookings, setBookings] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [cancellingId, setCancellingId] = useState<number | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const data = await apiFetch<BookingResponse[]>('/api/v1/bookings/me', { auth: true })
      setBookings(data)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to load bookings')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function handleCancel(id: number) {
    setCancellingId(id)
    try {
      await apiFetch(`/api/v1/bookings/${id}`, { method: 'DELETE', auth: true })
      await load()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to cancel booking')
    } finally {
      setCancellingId(null)
    }
  }

  return (
    <div className="page">
      <h1>My Bookings</h1>
      {loading && <p>Loading…</p>}
      {error && <p className="error">{error}</p>}
      {!loading && bookings.length === 0 && <p className="muted">You have no active bookings.</p>}

      <ul className="booking-list">
        {bookings.map((booking) => (
          <li key={booking.id} className="booking-card">
            <div>
              <strong>Seat {booking.seatLabel}</strong>
              <span className="muted"> · booked {new Date(booking.bookedAt).toLocaleString()}</span>
            </div>
            <button type="button" onClick={() => handleCancel(booking.id)} disabled={cancellingId === booking.id}>
              {cancellingId === booking.id ? 'Cancelling…' : 'Cancel'}
            </button>
          </li>
        ))}
      </ul>
    </div>
  )
}
