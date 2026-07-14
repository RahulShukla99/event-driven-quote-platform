package com.rahul.quoteplatform.pricingservice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.rahul.quoteplatform.common.event.QuoteValidatedEvent;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuotePricingProcessorTest {

    @Test
    void shouldCalculateTenPercentMarkup() {
        var processor = new QuotePricingProcessor(Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC));
        var result = processor.price(new QuoteValidatedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "customer@example.com",
                new BigDecimal("100.00"),
                Instant.parse("2026-07-14T10:00:00Z")));

        assertThat(result.price()).isEqualByComparingTo("110.00");
        assertThat(result.pricedAt()).isEqualTo(Instant.parse("2026-07-14T10:15:30Z"));
    }
}
