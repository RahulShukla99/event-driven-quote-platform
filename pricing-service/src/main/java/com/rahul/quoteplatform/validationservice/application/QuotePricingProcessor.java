package com.rahul.quoteplatform.pricingservice.application;

import com.rahul.quoteplatform.common.event.QuotePricedEvent;
import com.rahul.quoteplatform.common.event.QuoteValidatedEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class QuotePricingProcessor {

    private final Clock clock;

    public QuotePricingProcessor(Clock clock) {
        this.clock = clock;
    }

    public QuotePricedEvent price(QuoteValidatedEvent event) {
        var price = event.requestedAmount().multiply(new BigDecimal("1.10")).setScale(2, RoundingMode.HALF_UP);
        return new QuotePricedEvent(UUID.randomUUID(), event.quoteId(), event.customerId(), event.customerEmail(), event.requestedAmount(), price, Instant.now(clock));
    }
}
