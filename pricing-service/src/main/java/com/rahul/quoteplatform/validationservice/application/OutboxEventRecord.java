package com.rahul.quoteplatform.pricingservice.application;

import java.time.Instant;
import java.util.UUID;

public record OutboxEventRecord(UUID id, UUID aggregateId, String aggregateType, String eventType, String payloadJson, Instant createdAt, Instant publishedAt) {
}
