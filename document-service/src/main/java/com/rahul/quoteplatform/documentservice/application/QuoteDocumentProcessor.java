package com.rahul.quoteplatform.documentservice.application;

import com.rahul.quoteplatform.common.event.QuoteDocumentGeneratedEvent;
import com.rahul.quoteplatform.common.event.QuotePricedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class QuoteDocumentProcessor {

    private final Clock clock;

    public QuoteDocumentProcessor(Clock clock) {
        this.clock = clock;
    }

    public QuoteDocumentGeneratedEvent generate(QuotePricedEvent event) {
        return new QuoteDocumentGeneratedEvent(
                UUID.randomUUID(),
                event.quoteId(),
                event.customerId(),
                "https://documents.example/quotes/" + event.quoteId(),
                Instant.now(clock));
    }
}
