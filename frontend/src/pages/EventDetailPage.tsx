import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ApiError, apiFetch } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import type {
  BookingStrategy,
  CreateBookingRequest,
  CreateSeatRequest,
  EventResponse,
  SeatResponse,
} from '../api/types'

// The whole point of this project, surfaced in the UI: the same booking
// action can go through any of the three strategies BookingController
// exposes, so picking one and reading the note below it doubles as a
// walkthrough of what's actually being demonstrated.
const STRATEGY_INFO: Record<BookingStrategy, { label: string; path: string; description: string }> = {
  naive: {
    label: 'Naive',
    path: '/api/v1/bookings',
    description:
      'No locking - plain read-then-write. A single click here is safe, but under real concurrent load this is genuinely racy (see the JMeter results in loadtest/).',
  },
  pessimistic: {
    label: 'Pessimistic locking',
    path: '/api/v1/bookings/pessimistic',
    description:
      'Locks the seat row for the transaction (SELECT ... FOR UPDATE). Concurrent bookers queue up; whoever loses the race gets a clean 409, never a double-booking.',
  },
  optimistic: {
    label: 'Optimistic locking',
    path: '/api/v1/bookings/optimistic',
    description:
      "No lock held while waiting - relies on the seat's version column to reject a stale write with a 409 if someone else booked it a moment earlier.",
  },
}

const STRATEGY_KEYS = Object.keys(STRATEGY_INFO) as BookingStrategy[]

export function EventDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()

  const [event, setEvent] = useState<EventResponse | null>(null)
  const [seats, setSeats] = useState<SeatResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [strategy, setStrategy] = useState<BookingStrategy>('pessimistic')
  const [bookingSeatId, setBookingSeatId] = useState<number | null>(null)
  const [bookingMessage, setBookingMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  const [seatLabel, setSeatLabel] = useState('')
  const [addingSeat, setAddingSeat] = useState(false)
  const [seatFormError, setSeatFormError] = useState<string | null>(null)

  async function loadEventAndSeats() {
    if (!id) return
    setLoading(true)
    setError(null)
    try {
      const [eventData, seatData] = await Promise.all([
        apiFetch<EventResponse>(`/api/v1/events/${id}`),
        apiFetch<SeatResponse[]>(`/api/v1/events/${id}/seats`),
      ])
      setEvent(eventData)
      setSeats(seatData)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to load event')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadEventAndSeats()
    // Re-run only when the route param changes, not on every render.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  async function handleBook(seat: SeatResponse) {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: { pathname: `/events/${id}` } } })
      return
    }
    setBookingMessage(null)
    setBookingSeatId(seat.id)
    try {
      await apiFetch(STRATEGY_INFO[strategy].path, {
        method: 'POST',
        auth: true,
        body: { seatId: seat.id } satisfies CreateBookingRequest,
      })
      setBookingMessage({
        type: 'success',
        text: `Booked seat ${seat.seatLabel} (${STRATEGY_INFO[strategy].label}).`,
      })
      await loadEventAndSeats()
    } catch (err) {
      const text = err instanceof ApiError ? err.message : 'Booking failed'
      setBookingMessage({ type: 'error', text })
    } finally {
      setBookingSeatId(null)
    }
  }

  async function handleAddSeat(e: FormEvent) {
    e.preventDefault()
    if (!id) return
    setSeatFormError(null)
    setAddingSeat(true)
    try {
      await apiFetch<SeatResponse>(`/api/v1/events/${id}/seats`, {
        method: 'POST',
        body: { seatLabel } satisfies CreateSeatRequest,
      })
      setSeatLabel('')
      await loadEventAndSeats()
    } catch (err) {
      setSeatFormError(err instanceof ApiError ? err.message : 'Failed to add seat')
    } finally {
      setAddingSeat(false)
    }
  }

  if (loading) {
    return (
      <div className="page">
        <p>Loading…</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="page">
        <p className="error">{error}</p>
      </div>
    )
  }

  if (!event) return null

  return (
    <div className="page">
      <h1>{event.name}</h1>
      <p className="muted">
        {event.venue} · {new Date(event.eventTime).toLocaleString()}
      </p>

      <div className="strategy-picker card">
        <span>Booking strategy:</span>
        {STRATEGY_KEYS.map((key) => (
          <label key={key} className="radio">
            <input
              type="radio"
              name="strategy"
              value={key}
              checked={strategy === key}
              onChange={() => setStrategy(key)}
            />
            {STRATEGY_INFO[key].label}
          </label>
        ))}
        <p className="muted small">{STRATEGY_INFO[strategy].description}</p>
      </div>

      {bookingMessage && (
        <p className={bookingMessage.type === 'error' ? 'error' : 'success'}>{bookingMessage.text}</p>
      )}
      {!isAuthenticated && <p className="muted">Log in to book a seat.</p>}

      <ul className="seat-list">
        {seats.map((seat) => (
          <li key={seat.id} className={`seat-card ${seat.status === 'BOOKED' ? 'booked' : 'available'}`}>
            <span>{seat.seatLabel}</span>
            <span className="status">{seat.status}</span>
            <button
              type="button"
              onClick={() => handleBook(seat)}
              disabled={seat.status === 'BOOKED' || bookingSeatId === seat.id}
            >
              {bookingSeatId === seat.id ? 'Booking…' : seat.status === 'BOOKED' ? 'Booked' : 'Book'}
            </button>
          </li>
        ))}
        {seats.length === 0 && <p className="muted">No seats yet — add one below.</p>}
      </ul>

      <details className="card create-card">
        <summary>Add a seat</summary>
        <form onSubmit={handleAddSeat}>
          {seatFormError && <p className="error">{seatFormError}</p>}
          <label>
            Seat label
            <input
              value={seatLabel}
              onChange={(e) => setSeatLabel(e.target.value)}
              placeholder="e.g. A1"
              required
            />
          </label>
          <button type="submit" disabled={addingSeat}>
            {addingSeat ? 'Adding…' : 'Add seat'}
          </button>
        </form>
      </details>
    </div>
  )
}
