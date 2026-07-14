package com.rahul.quoteplatform.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuoteCreatedEvent(UUID eventId, UUID quoteId, String customerId, String customerEmail, BigDecimal requestedAmount, Instant occurredAt) {
}
