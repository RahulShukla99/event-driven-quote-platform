package com.rahul.quoteplatform.validationservice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.rahul.quoteplatform.common.event.QuoteCreatedEvent;
import com.rahul.quoteplatform.common.event.QuoteRejectedEvent;
import com.rahul.quoteplatform.common.event.QuoteValidatedEvent;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuoteValidationProcessorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC);

    @Test
    void shouldProduceQuoteValidatedEventForEligibleCustomer() {
        var processor = new QuoteValidationProcessor(CLOCK);
        var result = processor.validate(new QuoteCreatedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "customer@example.com",
                new BigDecimal("125.50"),
                Instant.parse("2026-07-14T10:00:00Z")));

        assertThat(result).isInstanceOf(QuoteValidatedEvent.class);
        var validatedEvent = (QuoteValidatedEvent) result;
        assertThat(validatedEvent.quoteId()).isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        assertThat(validatedEvent.validatedAt()).isEqualTo(Instant.parse("2026-07-14T10:15:30Z"));
    }

    @Test
    void shouldProduceQuoteRejectedEventForBlockedCustomer() {
        var processor = new QuoteValidationProcessor(CLOCK);
        var result = processor.validate(new QuoteCreatedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "BLOCKED-999",
                "customer@example.com",
                new BigDecimal("125.50"),
                Instant.parse("2026-07-14T10:00:00Z")));

        assertThat(result).isInstanceOf(QuoteRejectedEvent.class);
        var rejectedEvent = (QuoteRejectedEvent) result;
        assertThat(rejectedEvent.rejectionReason()).isEqualTo("Customer is not eligible");
        assertThat(rejectedEvent.rejectedAt()).isEqualTo(Instant.parse("2026-07-14T10:15:30Z"));
    }

    @Test
    void shouldProduceQuoteRejectedEventForMissingRequiredFields() {
        var processor = new QuoteValidationProcessor(CLOCK);
        var result = processor.validate(new QuoteCreatedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "",
                null,
                null,
                Instant.parse("2026-07-14T10:00:00Z")));

        assertThat(result).isInstanceOf(QuoteRejectedEvent.class);
        var rejectedEvent = (QuoteRejectedEvent) result;
        assertThat(rejectedEvent.rejectionReason()).isEqualTo("Required quote fields are missing");
    }
}
