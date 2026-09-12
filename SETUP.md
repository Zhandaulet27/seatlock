# SeatLock — Tools to Install

You're on Windows (PowerShell). Install links and notes below assume that;
swap in the macOS/Linux equivalents if that changes.

## Already have (from Event Manager)
- **Git** — used to push Event Manager to GitHub already.
- **Postman** — used to test Event Manager's CRUD endpoints already.
- **PostgreSQL** — used for Event Manager. Confirm you still have the service
  running (`services.msc` on Windows, look for `postgresql-x64-...`) and know
  your local superuser credentials.

If any of these aren't actually still set up, install/reinstall using the
links below.

## Needed for Phase 1 (core build)

1. **JDK 21 (LTS)** — the Java version to build on.
   - Eclipse Temurin: https://adoptium.net/temurin/releases/ (pick 21 - LTS,
     Windows, .msi installer)
   - Verify after install: `java -version` in PowerShell should show 21.

2. **IntelliJ IDEA Community Edition** — free, the standard IDE for Spring
   Boot work.
   - https://www.jetbrains.com/idea/download/
   - Has Spring Initializr built in (File → New → Project → Spring), or
     generate the skeleton at https://start.spring.io instead and open it.

3. **Maven** — usually not a separate install: Spring Initializr generates a
   project with `mvnw`/`mvnw.cmd` (the Maven wrapper) included, so you don't
   need Maven on your PATH. Only install standalone Maven
   (https://maven.apache.org/download.cgi) if you want the `mvn` command
   available globally.

4. **PostgreSQL** (if not already running) —
   https://www.postgresql.org/download/windows/ — the installer includes
   pgAdmin (GUI client) alongside the database server.

## Needed for Phase 2–4 (concurrency, auth, API docs)
No new installs — Spring Security, `@Version`/JPA locking, and
springdoc-openapi are all just dependencies added to the project
(`pom.xml`), not separate tools.

## Needed for Phase 5 (load testing)

5. **Apache JMeter** —
   https://jmeter.apache.org/download_jmeter.cgi (binary zip; requires a JDK,
   which you'll already have from step 1).
   - Extract it, run `bin\jmeter.bat` to launch the GUI.

## Needed for Phase 6 (containerizing)

6. **Docker Desktop for Windows** —
   https://www.docker.com/products/docker-desktop/
   - Needs WSL2 enabled (the installer will prompt you through this if it's
     not already on).
   - Verify after install: `docker --version` and `docker compose version`.

## Needed for Phase 7 (AWS deployment)

7. **An AWS account** — https://aws.amazon.com (free tier covers a small
   EC2 instance for a project like this — watch instance size/region to stay
   in the free tier).
8. **AWS CLI** —
   https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html
   (only needed if you want to script deployment from PowerShell; the AWS
   Console alone is enough to just click through provisioning an EC2 instance
   if you'd rather do that first).
9. An SSH client to reach the EC2 box — Windows 10/11 ships one
   (`ssh` works directly in PowerShell), so no separate install needed.

## Needed for Phase 8 (CI/CD)
No new installs — GitHub Actions runs in the cloud; you just add a
`.github/workflows/*.yml` file to the repo.

## Needed for Phase 9 (React frontend)

10. **Node.js (LTS)** — https://nodejs.org (includes npm).
    - Verify: `node -v` and `npm -v`.
11. Scaffold with Vite when you get there: `npm create vite@latest` (no
    separate install — `npm create` fetches it on demand).

## Suggested install order
Right now, before Phase 1: **JDK 21 → IntelliJ IDEA → confirm PostgreSQL is
running**. Everything else (JMeter, Docker Desktop, AWS account/CLI,
Node.js) you can install when you actually reach that phase — no need to set
it all up on day one.
