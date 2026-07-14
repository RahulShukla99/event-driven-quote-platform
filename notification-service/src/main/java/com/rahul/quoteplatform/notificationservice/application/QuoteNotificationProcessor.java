package com.rahul.quoteplatform.notificationservice.application;

import com.rahul.quoteplatform.common.event.QuoteDocumentGeneratedEvent;
import com.rahul.quoteplatform.common.event.QuoteNotificationSentEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class QuoteNotificationProcessor {

    private final Clock clock;

    public QuoteNotificationProcessor(Clock clock) {
        this.clock = clock;
    }

    public QuoteNotificationSentEvent send(QuoteDocumentGeneratedEvent event) {
        return new QuoteNotificationSentEvent(UUID.randomUUID(), event.quoteId(), event.customerId(), "email", Instant.now(clock));
    }
}
