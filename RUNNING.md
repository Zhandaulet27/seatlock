# Running the Phase 1 SeatLock backend

## 1. Unzip and open

Unzip `seatlock-backend.zip` somewhere permanent (not Downloads) — e.g.
`C:\dev\seatlock`. In IntelliJ IDEA: **File → Open** → select the `backend`
folder (the one containing `pom.xml`). IntelliJ recognizes it as a Maven
project automatically and downloads all dependencies using its own bundled
Maven — you don't need Maven or the wrapper installed separately for this.
First import will take a minute or two.

## 2. Create the database

Open a SQL client against your local PostgreSQL (psql, pgAdmin, or IntelliJ's
own Database tool window) and run:

```sql
CREATE DATABASE seatlock;
```

## 3. Point the app at your database

Open `src/main/resources/application.yml`. By default it expects username
`postgres` / password `postgres` on `localhost:5432`. Either:
- edit the file directly with your real local credentials, or
- leave the file as-is and set environment variables `DB_USERNAME` /
  `DB_PASSWORD` before running (matches the `${DB_USERNAME:postgres}` syntax
  already in the file).

## 4. Run it

In IntelliJ, open `SeatlockApplication.java` and click the green run arrow
next to `main`. First run creates all the tables automatically (that's what
`ddl-auto: update` in `application.yml` does).

You should see Spring Boot's startup banner and `Tomcat started on port 8080`.

## 5. Smoke-test with Postman

```
POST http://localhost:8080/api/v1/users
{ "email": "you@example.com" }
→ 201, note the returned "id"

POST http://localhost:8080/api/v1/events
{ "name": "Test Concert", "venue": "Arena", "eventTime": "2026-12-01T19:00:00" }
→ 201, note the returned "id" (this is the eventId)

POST http://localhost:8080/api/v1/events/{eventId}/seats
{ "seatLabel": "A1" }
→ 201, note the returned "id" (this is the seatId)

GET  http://localhost:8080/api/v1/events/{eventId}/seats
→ 200, shows seat A1 with status "AVAILABLE"

POST http://localhost:8080/api/v1/bookings
{ "seatId": <seatId>, "userId": <userId> }
→ 201, booking created

GET  http://localhost:8080/api/v1/events/{eventId}/seats
→ 200, seat A1 now shows status "BOOKED"

POST http://localhost:8080/api/v1/bookings   (same seatId again)
→ 409 Conflict, "Seat is not available"

DELETE http://localhost:8080/api/v1/bookings/{bookingId}
→ 204, seat freed back to AVAILABLE
```

If all of that works, Phase 1 is done.

## Known simplifications (intentional, not bugs)

- **No auth yet.** `userId` is passed directly in request bodies instead of
  coming from a login token. Phase 3 replaces this.
- **Booking under real concurrent load is still racy.** If two requests hit
  `POST /bookings` for the same seat at the exact same instant, you might see
  a raw 500 error instead of a clean 409 — the database's unique constraint on
  `booking.seat_id` stops the double-booking from actually happening, but the
  losing request doesn't get a nice error message yet. That's expected: it's
  exactly the problem Phase 2 (pessimistic/optimistic locking) fixes properly.
- **Relies on Spring Boot's "open session in view" default** (`open-in-view`,
  on by default) so lazy-loaded fields like `seat.event` can be read in the
  controller layer without extra fetch-join queries. Fine for a project this
  size; a larger app would usually turn this off and be more deliberate about
  fetching.
