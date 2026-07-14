package com.rahul.quoteplatform.common.event;

import java.time.Instant;
import java.util.UUID;

public record QuoteRejectedEvent(UUID eventId, UUID quoteId, String customerId, String rejectionReason, Instant rejectedAt) {
}
