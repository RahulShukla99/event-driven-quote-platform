package com.rahul.quoteplatform.validationservice.application;

import com.rahul.quoteplatform.common.event.QuoteCreatedEvent;
import com.rahul.quoteplatform.common.event.QuoteRejectedEvent;
import com.rahul.quoteplatform.common.event.QuoteValidatedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class QuoteValidationProcessor {

    private static final String BLOCKED_PREFIX = "BLOCKED-";
    private static final String REQUIRED_FIELDS_MESSAGE = "Required quote fields are missing";
    private static final String NOT_ELIGIBLE_MESSAGE = "Customer is not eligible";

    private final Clock clock;

    public QuoteValidationProcessor(Clock clock) {
        this.clock = clock;
    }

    public Object validate(QuoteCreatedEvent event) {
        if (hasMissingRequiredFields(event)) {
            return rejected(event, REQUIRED_FIELDS_MESSAGE);
        }
        if (event.customerId().startsWith(BLOCKED_PREFIX)) {
            return rejected(event, NOT_ELIGIBLE_MESSAGE);
        }
        return new QuoteValidatedEvent(UUID.randomUUID(), event.quoteId(), event.customerId(), event.customerEmail(), event.requestedAmount(), Instant.now(clock));
    }

    private boolean hasMissingRequiredFields(QuoteCreatedEvent event) {
        return event.customerId() == null || event.customerId().isBlank()
                || event.customerEmail() == null || event.customerEmail().isBlank()
                || event.requestedAmount() == null;
    }

    private QuoteRejectedEvent rejected(QuoteCreatedEvent event, String reason) {
        return new QuoteRejectedEvent(UUID.randomUUID(), event.quoteId(), event.customerId(), reason, Instant.now(clock));
    }
}
