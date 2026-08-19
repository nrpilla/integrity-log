# IntegrityLog
IntegrityLog is a proof-of-concept service for tamper-evident audit logging. It stores audit events in an append-only manner in PostgreSQL and links each record to the previous one using a SHA-256 hash chain. If data is changed directly in the database, verification detects the break.
## Purpose
This POC demonstrates:
- Append-only audit event storage
- Cryptographic hash chain integrity
- Chain verification without trusting raw database content alone
- A foundation for retention, redaction, and compliance reporting
## POC Scenarios
**Scenario A — Core integrity (in progress)**
Write and query audit events. Verify the full hash chain. Demonstrate tamper detection by modifying a row outside the application.
**Scenario B — Retention and redaction (planned)**
Archive or expire old events. Redact sensitive payload data while keeping the hash chain valid using a payload digest.
**Scenario C — Compliance clarification (planned)**
Document interpretation of an ambiguous compliance requirement and expose a simple compliance report API.
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
2. From the repository root, start PostgreSQL using Docker Compose. This creates a database named integritylog with the credentials configured in the application properties file.
3. Open the integrity-log-service module. This is the Spring Boot application.
4. Run the application using Maven or the Maven Wrapper. On first startup, Flyway applies the initial database migration and creates the audit_event table.
5. Confirm the application started successfully. By default it listens on port 8080. If that port is in use, change the server port in application.properties.
6. Build the project with Maven to compile and run tests.
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
## Planned API (Scenario A)
| Method | Path | Description |
|--------|------|-------------|
| POST | /audit/events | Append a new audit event |
| GET | /audit/events | Query events (e.g. by resource) |
| GET | /audit/events/{id} | Get a single event |
| GET | /audit/verify | Verify integrity of the full chain |

Implementation note: the API accepts a structured JSON payload object and assigns timestamps server-side at `created_at` in PostgreSQL. Caller-supplied timestamps are not used to preserve a trusted audit trail.

## Tamper Demo (Scenario A)
After events are written through the API, verification should report a valid chain. Directly updating an event row in PostgreSQL (outside the application) should cause verification to fail and identify where the chain broke. This shows that integrity checks detect unauthorized database changes.
## Windows Timezone Note
On some Windows setups, PostgreSQL may reject the legacy JVM timezone Asia/Calcutta. This project sets the JVM timezone to UTC in the Maven build configuration. If you run the application from an IDE, add the same timezone setting to the run configuration VM options.
## Current Status
- Project setup complete: Spring Boot application, PostgreSQL, Flyway migration
- Application runs locally
- Scenario A (hash chain and REST APIs) — not yet implemented
- Scenario B and C — planned
