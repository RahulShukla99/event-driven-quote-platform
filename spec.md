# event-driven-quote-platform

A production-minded, event-driven quote-processing platform demonstrating reliable
distributed systems patterns using Kafka. Modeled conceptually on real quote-management
platform work (Visable), rebuilt from scratch as a clean reference implementation.

---

## 1. Goal

Build a realistic quote-processing workflow that goes beyond a toy pub/sub demo and
actually demonstrates the reliability patterns expected of a production system:
idempotency, retries, dead-letter handling, exactly-once-ish delivery via transactional
outbox, schema evolution, and observability.

**Guiding principle:** ship a smaller system that fully works end-to-end with all
reliability patterns in place, rather than a larger system that's half-finished.

---

## 2. Business Workflow

```text
API receives quote request
        ↓
   [QuoteCreated event]
        ↓
   Validation Service
        ↓
   [QuoteValidated / QuoteRejected event]
        ↓
   Pricing Service
        ↓
   [QuotePriced event]
        ↓
   Document Generation Service
        ↓
   [QuoteDocumentGenerated event]
        ↓
   Notification Service
        ↓
   [QuoteNotificationSent event]
```

Each arrow is a Kafka topic. Each box is an independently deployable service
(separate Spring Boot app or module, depending on time budget — see Section 9).

---

## 3. Services

| Service | Responsibility | Consumes | Produces |
|---|---|---|---|
| **Quote API** | Accepts inbound quote requests via REST, writes to outbox | — | `QuoteCreated` |
| **Validation Service** | Validates quote data (business rules, required fields, customer eligibility) | `QuoteCreated` | `QuoteValidated`, `QuoteRejected` |
| **Pricing Service** | Calculates price based on quote parameters | `QuoteValidated` | `QuotePriced` |
| **Document Service** | Generates a quote PDF/document | `QuotePriced` | `QuoteDocumentGenerated` |
| **Notification Service** | Sends email/notification to customer | `QuoteDocumentGenerated` | `QuoteNotificationSent` |

---

## 4. Core Reliability Patterns (the actual point of this project)

### 4.1 Transactional Outbox
- Each service writes its state change **and** the outbound event to the same local
  DB transaction (an `outbox` table), instead of publishing directly to Kafka inline.
- A separate poller (or Debezium/CDC in a stretch version) reads the outbox table and
  publishes to Kafka, marking rows as sent.
- **Why:** avoids the classic "DB commit succeeded, Kafka publish failed" inconsistency.

### 4.2 Idempotent Consumers
- Every consumer checks a `processed_events` table (keyed by event ID) before acting.
- If the event ID has already been processed, the consumer acknowledges and skips —
  no duplicate side effects (e.g., no duplicate pricing calculation, no duplicate email).

### 4.3 Duplicate-Event Protection
- Event ID (UUID) generated at creation time, carried through the whole event chain.
- Combined with 4.2's idempotency table, this covers both "Kafka redelivered the same
  message" and "producer accidentally sent twice."

### 4.4 Retries
- Consumer-side retry with exponential backoff for transient failures (e.g., DB
  connection blip, downstream service temporarily unavailable).
- Configurable max retry count per consumer (e.g., 3 attempts).

### 4.5 Dead-Letter Queue (DLQ)
- After exhausting retries, the event is published to a `<topic>.DLQ` topic with
  failure metadata (exception, timestamp, retry count).
- A small DLQ inspection endpoint/CLI to view and optionally replay DLQ messages.

### 4.6 Schema Versioning
- Avro schemas registered in Confluent Schema Registry (or a lightweight local
  equivalent if running fully self-hosted without Confluent Cloud).
- Demonstrate one **backward-compatible schema evolution** (e.g., adding an optional
  field to `QuotePriced` in a v2 schema) and show old consumers still work.

### 4.7 Observability
- Structured logging with correlation/trace ID propagated through every event
  (carried as a Kafka header, not just in the payload).
- Basic metrics: events produced/consumed per topic, consumer lag, DLQ count,
  processing latency per stage.
- OpenTelemetry instrumentation if time allows; otherwise Micrometer + Prometheus
  format logs as a minimum viable version.

### 4.8 Integration Tests
- Testcontainers-based integration tests spinning up real Kafka (and Postgres) in
  Docker for each test run — not mocked producers/consumers.
- At minimum: one full happy-path test (quote created → notification sent) and one
  failure-path test (validation fails → `QuoteRejected` → workflow stops correctly).

---

## 5. Tech Stack

- **Language/Framework:** Java 21, Spring Boot 3.5 (consistent with your existing
  OIDC project — reuse familiarity, don't context-switch stacks)
- **Messaging:** Apache Kafka (via Docker Compose locally)
- **Schema Registry:** Confluent Schema Registry (Docker) + Avro
- **Database:** PostgreSQL (one schema per service, or shared instance with logical
  separation for local dev simplicity)
- **Testing:** Testcontainers, JUnit 5
- **Observability:** Micrometer + Prometheus + Grafana (Docker Compose), optionally
  OpenTelemetry if time allows
- **Build:** Maven or Gradle (pick whichever you already default to)
- **Orchestration (local):** Docker Compose — Kafka, Zookeeper/KRaft, Schema Registry,
  Postgres, Prometheus, Grafana all spun up with one `docker-compose up`

---

## 6. Repository Structure (proposed)

```text
event-driven-quote-platform/
├── docker-compose.yml
├── README.md
├── spec.md
├── quote-api/
├── validation-service/
├── pricing-service/
├── document-service/
├── notification-service/
├── common/                  # shared event schemas, correlation-id utils, outbox lib
├── schemas/                 # Avro schema definitions (versioned)
└── docs/
    ├── architecture-diagram.png
    └── event-flow.md
```

---

## 7. Milestones (timeboxed — see Section 9 for rationale)

### Milestone 1 — Core happy path (Week 1)
- [ ] Docker Compose: Kafka + Postgres running locally
- [ ] Quote API → `QuoteCreated` (with transactional outbox)
- [ ] Validation Service consumes, produces `QuoteValidated`
- [ ] Pricing Service consumes, produces `QuotePriced`
- [ ] End-to-end happy path works, observable via logs

### Milestone 2 — Reliability patterns (Week 2)
- [ ] Idempotent consumers (processed_events table) on all services
- [ ] Retry with backoff on all consumers
- [ ] DLQ topic + basic DLQ viewer
- [ ] Document + Notification services complete the full chain

### Milestone 3 — Schema versioning + observability (Week 3)
- [ ] Schema Registry integration, Avro schemas for all events
- [ ] One demonstrated backward-compatible schema evolution (v1 → v2)
- [ ] Metrics dashboard (Grafana): consumer lag, DLQ count, latency per stage
- [ ] Correlation ID propagated end-to-end, visible in a trace/log view

### Milestone 4 — Tests + polish (stretch, if time allows)
- [ ] Testcontainers integration tests (happy path + failure path)
- [ ] README with architecture diagram and "how to run locally" instructions
- [ ] Short write-up: design decisions, trade-offs, what you'd do differently at scale

**If time runs out:** Milestones 1–2 alone are still a legitimate, demoable project.
Milestone 3–4 are what take it from "good" to "clearly senior."

---

## 8. Resume/README Framing (draft — refine once built)

> Designed and built a production-minded, event-driven quote-processing platform
> (Spring Boot 3.5/Java 21, Kafka) modeling a multi-stage workflow — validation,
> pricing, document generation, notification — with transactional outbox, idempotent
> consumers, retry/DLQ handling, and Avro schema versioning via Schema Registry;
> instrumented with correlation-ID tracing and Prometheus/Grafana dashboards.

Keep this in your own words when you actually write it — don't lift phrasing from
any reference letter or prior document.

---

## 9. Honest Scope Note

This spec is intentionally larger than a weekend project. Given active interview
loops running concurrently, the realistic approach is:

1. Scaffold the repo + README/architecture diagram immediately (shareable even if
   incomplete).
2. Build Milestone 1 first — a working, if minimal, end-to-end flow.
3. Layer in Milestone 2 (idempotency/retry/DLQ) next — this is the part that actually
   demonstrates production thinking, so don't skip it in favor of Milestone 3's
   schema versioning polish if time is tight.
4. Treat Milestones 3–4 as stretch goals to add incrementally after initial interviews,
   not blockers to having something to show now.
