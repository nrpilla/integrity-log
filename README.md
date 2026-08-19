# IntegrityLog
IntegrityLog is a proof-of-concept service for tamper-evident audit logging. It stores audit events in an append-only manner in PostgreSQL and links each record to the previous one using a SHA-256 hash chain. If data is changed directly in the database, verification detects the break.
## Purpose
This POC demonstrates:
- Append-only audit event storage
- Cryptographic hash chain integrity
- Chain verification without trusting raw database content alone
- A foundation for retention, redaction, and compliance reporting
## POC Scenarios
**Scenario A — Core integrity**
Write and query audit events; verify the full hash chain and demonstrate tamper detection by modifying a row outside the application.
**Scenario B — Retention and redaction**
Archive or expire old events and redact sensitive payload data while preserving chain integrity using payload digests and redaction records.
**Scenario C — Compliance reporting**
Implemented pieces in this repository:
- /audit/export endpoint that returns JSON exports and supports filtering (actorId, resourceId)
- A basic compliance report that summarizes event counts, per-resource summaries, and verification status
- Export hooks to generate artifacts suitable for compliance review
- Unit and integration tests covering export and report generation
These components provide a foundation for compliance workflows and for building further automation and documentation.
## Technology
- Java 21
- Spring Boot 4
- Spring Web (REST)
- Spring Data JPA
- PostgreSQL 16
- Flyway (database migrations)
- Maven
- Docker Compose (local PostgreSQL)
## Prerequisites
Before running the project locally, install:
- Java 21
- Apache Maven (or use the Maven Wrapper included in the service module)
- Docker Desktop with Docker Compose
- Git
## Repository Layout
- **integrity-log/** — repository root
- **README.md** — this file
- **docker-compose.yml** — local PostgreSQL
- **integrity-log-service/** — Spring Boot application
- Application source and configuration
- Flyway migration for the audit_event table
- Maven build files
## Getting Started
1. Clone the repository from GitHub to your local machine.
2. From the repository root, start PostgreSQL using Docker Compose:

   docker compose up -d

   This creates a database named `integritylog` with the credentials from application.properties.
3. Build and run the service (from repo root):

   mvn -f integrity-log-service/pom.xml spring-boot:run

   Or package and run the jar:

   mvn -f integrity-log-service/pom.xml clean package
   java -jar integrity-log-service/target/*.jar
4. On first startup Flyway applies migrations and creates the `audit_event` table.
5. The service listens on port 8080 by default (integrity-log-service/src/main/resources/application.properties).
6. Quick example: append an event and verify the chain:

   curl -X POST http://localhost:8080/audit/events -H "Content-Type: application/json" -d '{"actorId":"user-1","resourceType":"account","resourceId":"acct-42","eventType":"update","payload":{}}'
   curl http://localhost:8080/audit/verify

7. Run tests for the service module:

   mvn -f integrity-log-service/pom.xml test

## Configuration
Database connection settings are in integrity-log-service/src/main/resources/application.properties. The application expects PostgreSQL on localhost port 5432.
Flyway is enabled so schema changes are applied automatically on startup. Do not modify migration files after they have been applied; add a new versioned migration for schema changes.
## Hash Chain Design (Summary)
Each audit event includes:
- **content_hash** — hash of the canonical event content
- **previous_hash** — record hash of the prior event (a fixed genesis value is used for the first event)
- **record_hash** — hash of the previous hash and content hash combined
Events are ordered by sequence_number. Verification walks the chain in order and recomputes hashes. Any mismatch indicates tampering or corruption.
Detailed design notes can be documented separately in DESIGN.md when needed.
## API (implemented)
The service exposes the following audit endpoints under /audit:

| Method | Path | Description |
|--------|------|-------------|
| POST | /audit/events | Append a new audit event (creates sequence/hash links) |
| GET | /audit/events | Query events (filters: actorId, resourceType, resourceId, eventType, from, to; paging: page, size) |
| GET | /audit/events/{id} | Get a single event by UUID |
| GET | /audit/verify | Verify integrity of the full hash chain |
| POST | /audit/events/{id}/archive | Archive a specific event (idempotent) |
| POST | /audit/events/{id}/redact | Redact fields of an event (creates a new redacted record) |
| GET | /audit/export | Export events (optional actorId/resourceId filters) |
| POST | /audit/access | Record client access audit entries |
| GET | /audit/access | Query client access audit entries (filters similar to events)

Notes:
- The API accepts JSON payloads and assigns trusted server-side timestamps (`created_at`) in PostgreSQL; caller timestamps are ignored.
- Querying supports ISO-8601 date-time for `from` and `to` parameters and paging via `page` and `size` (size limited to 1–100).
## Tamper Demo (Scenario A)
After events are written through the API, verification should report a valid chain. Directly updating an event row in PostgreSQL (outside the application) should cause verification to fail and identify where the chain broke. This shows that integrity checks detect unauthorized database changes.
## Windows Timezone Note
On some Windows setups, PostgreSQL may reject the legacy JVM timezone Asia/Calcutta. This project sets the JVM timezone to UTC in the Maven build configuration. If you run the application from an IDE, add the same timezone setting to the run configuration VM options.
## Current Status
- Project setup complete: Spring Boot application, PostgreSQL, Flyway migration
- Scenarios A and B implemented and tested: core integrity and retention/redaction are functional
- Scenario C implemented components: /audit/export endpoint (JSON exports with filters), a basic compliance report summarizing event counts and verification status, export hooks for compliance artifacts, and tests covering export/report generation
- Integration and unit tests for controller and verification logic present and passing (run `mvn -f integrity-log-service/pom.xml test`)
- Documentation: README updated with endpoints and run instructions; consider adding operational runbooks and compliance artifact exports for production use
- This repository remains a proof-of-concept; production hardening (key management, long-term sealing, audit proof publication) is outside its current scope.

