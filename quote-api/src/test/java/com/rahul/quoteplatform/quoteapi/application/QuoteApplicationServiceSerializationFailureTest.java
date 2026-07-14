package com.rahul.quoteplatform.quoteapi.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class QuoteApplicationServiceSerializationFailureTest {

    @Test
    void shouldFailWhenOutboxPayloadCannotBeSerialized() throws Exception {
        var quoteRepository = mock(QuoteRepository.class);
        var outboxRepository = mock(OutboxEventRepository.class);
        var objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") {
        });
        var service = new QuoteApplicationService(quoteRepository, outboxRepository, Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC), objectMapper);

        assertThatThrownBy(() -> service.createQuote(new CreateQuoteCommand("cust-123", "customer@example.com", new BigDecimal("125.50"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to serialize quote created event");
    }
}
