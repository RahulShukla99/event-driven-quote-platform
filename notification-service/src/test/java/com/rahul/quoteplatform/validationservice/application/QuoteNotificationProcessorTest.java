package com.rahul.quoteplatform.notificationservice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.rahul.quoteplatform.common.event.QuoteDocumentGeneratedEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuoteNotificationProcessorTest {

    @Test
    void shouldCreateNotificationSentEvent() {
        var processor = new QuoteNotificationProcessor(Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC));
        var result = processor.send(new QuoteDocumentGeneratedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "https://documents.example/quotes/22222222-2222-2222-2222-222222222222",
                Instant.parse("2026-07-14T10:10:00Z")));

        assertThat(result.quoteId()).isEqualTo(UUID.fromString("22222222-2222-2222-2222-222222222222"));
        assertThat(result.channel()).isEqualTo("email");
        assertThat(result.sentAt()).isEqualTo(Instant.parse("2026-07-14T10:15:30Z"));
    }
}
