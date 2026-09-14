import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ApiError, apiFetch } from '../api/client'
import type { CreateEventRequest, EventResponse } from '../api/types'

// GET/POST /api/v1/events are both public (permitAll in SecurityConfig) -
// no login required to browse events or create one, only to book a seat.
export function EventsPage() {
  const [events, setEvents] = useState<EventResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [name, setName] = useState('')
  const [venue, setVenue] = useState('')
  const [eventTime, setEventTime] = useState('')
  const [creating, setCreating] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  async function loadEvents() {
    setLoading(true)
    setError(null)
    try {
      const data = await apiFetch<EventResponse[]>('/api/v1/events')
      setEvents(data)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to load events')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadEvents()
  }, [])

  async function handleCreate(e: FormEvent) {
    e.preventDefault()
    setFormError(null)
    setCreating(true)
    try {
      // <input type="datetime-local"> yields "YYYY-MM-DDTHH:mm" (no seconds);
      // the backend's LocalDateTime parser wants "YYYY-MM-DDTHH:mm:ss".
      const normalizedTime = eventTime.length === 16 ? `${eventTime}:00` : eventTime
      await apiFetch<EventResponse>('/api/v1/events', {
        method: 'POST',
        body: { name, venue, eventTime: normalizedTime } satisfies CreateEventRequest,
      })
      setName('')
      setVenue('')
      setEventTime('')
      await loadEvents()
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : 'Failed to create event')
    } finally {
      setCreating(false)
    }
  }

  return (
    <div className="page">
      <h1>Events</h1>

      {loading && <p>Loading events…</p>}
      {error && <p className="error">{error}</p>}
      {!loading && !error && events.length === 0 && (
        <p className="muted">No events yet — create one below to get started.</p>
      )}

      <ul className="event-list">
        {events.map((event) => (
          <li key={event.id} className="event-card">
            <Link to={`/events/${event.id}`}>
              <strong>{event.name}</strong>
              <span>{event.venue}</span>
              <span className="muted">{new Date(event.eventTime).toLocaleString()}</span>
            </Link>
          </li>
        ))}
      </ul>

      <details className="card create-card">
        <summary>Create an event</summary>
        <form onSubmit={handleCreate}>
          {formError && <p className="error">{formError}</p>}
          <label>
            Name
            <input value={name} onChange={(e) => setName(e.target.value)} required />
          </label>
          <label>
            Venue
            <input value={venue} onChange={(e) => setVenue(e.target.value)} required />
          </label>
          <label>
            Date &amp; time
            <input
              type="datetime-local"
              value={eventTime}
              onChange={(e) => setEventTime(e.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={creating}>
            {creating ? 'Creating…' : 'Create event'}
          </button>
        </form>
      </details>
    </div>
  )
}
