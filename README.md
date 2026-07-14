# event-driven-quote-platform

## Implemented scope

- Quote API with transactional outbox
- Validation, Pricing, Document, and Notification services
- Idempotent consumers with processed-event tracking
- Retry with exponential backoff
- DLQ publishing after retries are exhausted

## Local run

1. Start infrastructure:

```bash
docker compose up -d
```

2. Run the services in separate terminals:

```bash
cd quote-api && mvn spring-boot:run
cd validation-service && mvn spring-boot:run
cd pricing-service && mvn spring-boot:run
cd document-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

Or start them one by one in separate shells after Docker is up.

## Happy path

1. Create a quote:

```bash
curl -X POST http://localhost:8080/quotes \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-123","customerEmail":"customer@example.com","requestedAmount":125.50}'
```

2. Watch these topics:
- `quote.created`
- `quote.validated`
- `quote.priced`
- `quote.document.generated`
- `quote.notification.sent`

## Duplicate event proof

Re-send the exact same Kafka message with the same `eventId` to the input topic for a consumer.

Expected result:
- only one row in `*_processed_events`
- no second outbox row
- second delivery is skipped

## DLQ proof

Stop PostgreSQL for one service, then re-send a message to that service’s input topic.

Expected result after 3 attempts:
- the consumer stops retrying
- a message appears on `<source-topic>.DLQ`
- DLQ payload includes exception message, timestamp, and retry count

## Topic mapping

- `quote.created` → Validation Service
- `quote.validated` → Pricing Service
- `quote.priced` → Document Service
- `quote.document.generated` → Notification Service
- `quote.notification.sent` → final event

## DLQ message shape

```json
{
  "eventId": "...",
  "sourceTopic": "quote.validated",
  "payloadJson": "{...}",
  "exceptionMessage": "db down",
  "failedAt": "2026-07-14T10:15:30Z",
  "retryCount": 3
}
```