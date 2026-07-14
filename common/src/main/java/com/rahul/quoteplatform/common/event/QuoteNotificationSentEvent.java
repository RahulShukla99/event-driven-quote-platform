package com.rahul.quoteplatform.common.event;

import java.time.Instant;
import java.util.UUID;

public record QuoteNotificationSentEvent(UUID eventId, UUID quoteId, String customerId, String channel, Instant sentAt) {
}
