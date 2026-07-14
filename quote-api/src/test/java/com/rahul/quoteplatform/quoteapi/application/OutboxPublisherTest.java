package com.rahul.quoteplatform.quoteapi.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class OutboxPublisherTest {

    @Test
    void shouldPublishUnsentOutboxEventAndMarkItAsPublished() {
        var eventId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        var now = Instant.parse("2026-07-14T10:15:30Z");
        var outboxRepository = new InMemoryOutboxEventRepository(List.of(new OutboxEventRecord(
                eventId,
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Quote",
                "QuoteCreated",
                "{\"customerId\":\"cust-123\"}",
                now,
                null)));
        var kafkaTemplate = mock(KafkaTemplate.class);
        var clock = Clock.fixed(now, ZoneOffset.UTC);
        var publisher = new OutboxPublisher(outboxRepository, kafkaTemplate, clock);

        publisher.publishPendingEvents();

        verify(kafkaTemplate).send("quote.created", "{\"customerId\":\"cust-123\"}");
        assertThat(outboxRepository.publishedEventIds).containsExactly(eventId);
        assertThat(outboxRepository.publishedAt).isEqualTo(now);
    }

    private static final class InMemoryOutboxEventRepository implements OutboxEventRepository {
        private final List<OutboxEventRecord> events;
        private List<UUID> publishedEventIds = new ArrayList<>();
        private Instant publishedAt;

        private InMemoryOutboxEventRepository(List<OutboxEventRecord> events) {
            this.events = new ArrayList<>(events);
        }

        @Override
        public void save(OutboxEventRecord event) {
            events.add(event);
        }

        @Override
        public List<OutboxEventRecord> findUnpublished(int limit) {
            return events.stream().filter(event -> event.publishedAt() == null).limit(limit).toList();
        }

        @Override
        public void markPublished(List<UUID> eventIds, Instant publishedAt) {
            this.publishedEventIds = new ArrayList<>(eventIds);
            this.publishedAt = publishedAt;
        }
    }
}
