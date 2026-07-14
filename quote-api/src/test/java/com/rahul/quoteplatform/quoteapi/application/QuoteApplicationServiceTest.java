package com.rahul.quoteplatform.quoteapi.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuoteApplicationServiceTest {

    @Test
    void shouldCreateQuoteAndWriteOutboxEventInTheSameUseCase() {
        var quoteRepository = new InMemoryQuoteRepository();
        var outboxRepository = new InMemoryOutboxRepository();
        var clock = Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC);
        var service = new QuoteApplicationService(quoteRepository, outboxRepository, clock, new ObjectMapper().findAndRegisterModules());

        var response = service.createQuote(new CreateQuoteCommand("cust-123", "customer@example.com", new BigDecimal("125.50")));

        assertThat(response.quoteId()).isNotNull();
        assertThat(quoteRepository.quotes).hasSize(1);
        assertThat(quoteRepository.quotes.getFirst().customerId()).isEqualTo("cust-123");
        assertThat(outboxRepository.events).hasSize(1);
        assertThat(outboxRepository.events.getFirst().eventType()).isEqualTo("QuoteCreated");
        assertThat(outboxRepository.events.getFirst().aggregateId()).isEqualTo(response.quoteId());
        assertThat(outboxRepository.events.getFirst().payloadJson()).contains("cust-123");
        assertThat(outboxRepository.events.getFirst().payloadJson()).contains("125.50");
        assertThat(outboxRepository.events.getFirst().createdAt()).isEqualTo(Instant.parse("2026-07-14T10:15:30Z"));
    }

    private static final class InMemoryQuoteRepository implements QuoteRepository {
        private final List<QuoteRecord> quotes = new ArrayList<>();

        @Override
        public void save(QuoteRecord quote) {
            quotes.add(quote);
        }
    }

    private static final class InMemoryOutboxRepository implements OutboxEventRepository {
        private final List<OutboxEventRecord> events = new ArrayList<>();

        @Override
        public void save(OutboxEventRecord event) {
            events.add(event);
        }
    }
}
