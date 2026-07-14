package com.rahul.quoteplatform.common.event;

import java.time.Instant;
import java.util.UUID;

public record DeadLetterMessage(UUID eventId, String sourceTopic, String payloadJson, String exceptionMessage, Instant failedAt, int retryCount) {
}
