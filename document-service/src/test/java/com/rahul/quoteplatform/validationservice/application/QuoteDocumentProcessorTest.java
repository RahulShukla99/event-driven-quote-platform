package com.rahul.quoteplatform.documentservice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.rahul.quoteplatform.common.event.QuotePricedEvent;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuoteDocumentProcessorTest {

    @Test
    void shouldCreateDocumentGeneratedEvent() {
        var processor = new QuoteDocumentProcessor(Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC));
        var result = processor.generate(new QuotePricedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "customer@example.com",
                new BigDecimal("100.00"),
                new BigDecimal("110.00"),
                Instant.parse("2026-07-14T10:00:00Z")));

        assertThat(result.quoteId()).isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        assertThat(result.documentUrl()).contains("22222222-2222-2222-2222-222222222222");
        assertThat(result.generatedAt()).isEqualTo(Instant.parse("2026-07-14T10:15:30Z"));
    }
}
