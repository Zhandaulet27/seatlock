# SeatLock — Concurrency-Safe Event Booking Platform

A solo resume project: a ticket/seat booking backend (Java + Spring Boot) with a
React frontend, built to demonstrate real backend engineering — correctness under
concurrent load, security, testing, containerization, and deployment — for
applications to Google, Amazon, Meta, Netflix, Microsoft, Bloomberg and similar.

The differentiator over a generic CRUD app is Phase 2 below: proving, with a
load test, that two people can't book the same seat at the same time.

---

## 1. Tech stack

| Layer          | Choice                                                        |
|----------------|-----------------------------------------------------------------|
| Language       | Java 21 (LTS)                                                  |
| Framework      | Spring Boot 3.x (Web, Data JPA, Security, Validation)          |
| Database       | PostgreSQL                                                     |
| Auth           | Spring Security + JWT                                          |
| API docs       | springdoc-openapi (Swagger UI)                                 |
| Load testing   | Apache JMeter                                                  |
| Containers     | Docker + Docker Compose                                        |
| Deployment     | AWS EC2 + Nginx (reverse proxy)                                 |
| CI/CD          | GitHub Actions                                                  |
| Frontend       | React                                                            |

You already have Java, Git, SQL basics and REST fundamentals from the
Event Manager practice project. Everything below builds on that.

---

## 2. Domain model (v1)

```
User        (id, email, password_hash, role, created_at)
Event       (id, name, venue, event_time, created_at)
Seat        (id, event_id -> Event, seat_label, status, version)
Booking     (id, seat_id -> Seat, user_id -> User, status, booked_at)
```

Notes:
- `Seat.status`: `AVAILABLE` / `BOOKED` — this is the field every concurrent
  request is racing to change, so it's the center of the whole project.
- `Seat.version`: an integer column used by JPA's `@Version` for optimistic
  locking (see Phase 2).
- Add a **unique constraint** on `Booking(seat_id)` where status is active, or
  simpler for v1: a unique constraint on `Seat.id` + a `CHECK`/application-level
  guard that a seat can only have one non-cancelled booking. This constraint is
  your last line of defense if the locking logic ever has a bug — the database
  itself should refuse a double-booking, not just the app.

---

## 3. API sketch (v1)

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login              -> returns JWT

GET    /api/v1/events
GET    /api/v1/events/{id}/seats

POST   /api/v1/bookings                -> { seatId }  (auth required)
GET    /api/v1/bookings/me             (auth required)
DELETE /api/v1/bookings/{id}           (auth required, owner only)
```

`POST /bookings` is the endpoint everything else in this project exists to
protect — it's where the locking strategy lives.

---

## 4. Build roadmap (phased)

Build in this order. Each phase should be a separate set of commits / a
separate short-lived branch, so your GitHub history itself demonstrates
incremental, tested work — which is also a good interview talking point.

### Phase 1 — Core CRUD
Entities, repositories, controllers for Event/Seat/Booking. Plain REST, no
auth yet, no concurrency handling yet. Goal: get the shape of the app working
end-to-end, the same way Event Manager did.

**Done when:** you can create an event, list its seats, and book one via
Postman, backed by a real PostgreSQL database.

### Phase 2 — Concurrency control (the core of the project)
Implement **two** booking strategies so you can compare and explain both in
an interview:
- **Pessimistic locking**: `@Lock(LockModeType.PESSIMISTIC_WRITE)` on the seat
  query inside the booking transaction — the DB row is locked until the
  transaction commits, so a second concurrent request blocks and waits.
- **Optimistic concurrency**: `@Version` column on `Seat`; a concurrent update
  throws `OptimisticLockException`, which you catch and turn into a clean
  "seat no longer available" response (optionally with a retry).

Write a unit/integration test that fires many simultaneous booking requests at
the same seat (e.g. with `ExecutorService` + `CountDownLatch` in a JUnit test)
and asserts exactly one succeeds.

**Done when:** that test passes reliably, for both strategies.

### Phase 3 — Auth
Spring Security + JWT: register/login, password hashing (BCrypt), securing
`/bookings/**` so only authenticated users can book, and `DELETE` only allowed
by the booking's owner.

**Done when:** an unauthenticated request to book is rejected, and a user
can't cancel someone else's booking.

### Phase 4 — API polish
API versioning (`/api/v1/...` — already reflected in the sketch above),
input validation (`@Valid` + Bean Validation), consistent error responses,
and springdoc-openapi wired in so `/swagger-ui.html` works.

### Phase 5 — Load testing
Install JMeter, build a test plan that simulates 500+ concurrent booking
requests against the same event/seat pool, and run it against both booking
strategies. Record actual throughput and confirm zero double-bookings.
**This is where the "load-tested to 500+ simultaneous bookings" resume line
becomes true — write it only after you've run this and have the numbers.**

### Phase 6 — Containerize
Dockerfile for the Spring Boot app, `docker-compose.yml` wiring app +
PostgreSQL together, so the whole thing runs with `docker compose up`.

### Phase 7 — Deploy
Provision an EC2 instance, run the Docker Compose stack there, put Nginx in
front as a reverse proxy (and TLS via Let's Encrypt if you want it fully
production-shaped).

### Phase 8 — CI/CD
GitHub Actions workflow: build + run tests on every push; optionally deploy
to EC2 on merge to `main`.

### Phase 9 — React frontend
Build once the API is stable: event list, seat map, login, booking flow.
Deliberately last so you're not building UI against an API that's still
changing shape.

---

## 5. What "done" looks like for the resume bullet

Don't write the final bullet until you can back every clause with something
real in the repo:
- "prevents double-booking under concurrent requests" → Phase 2 test passing.
- "load-tested to 500+ simultaneous bookings with JMeter" → Phase 5 results.
- "versioned, Spring Security/JWT-secured REST API ... OpenAPI/Swagger" →
  Phase 3 + 4.
- "containerized ... deployed on AWS EC2 behind Nginx with GitHub Actions
  CI/CD" → Phases 6–8.

See `SETUP.md` for the tools to install before starting Phase 1.
