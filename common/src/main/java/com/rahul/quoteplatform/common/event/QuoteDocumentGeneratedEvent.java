package com.rahul.quoteplatform.common.event;

import java.time.Instant;
import java.util.UUID;

public record QuoteDocumentGeneratedEvent(UUID eventId, UUID quoteId, String customerId, String documentUrl, Instant generatedAt) {
}
