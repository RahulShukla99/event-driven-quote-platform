package com.rahul.quoteplatform.notificationservice.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteDocumentGeneratedEvent;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuoteNotificationWorkflowTest {

    @Test
    void shouldSaveProcessedEventAndOutboxEventForGeneratedDocument() {
        var clock = Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC);
        var processedEventRepository = new InMemoryProcessedEventRepository();
        var outboxRepository = new InMemoryOutboxEventRepository();
        var processor = new QuoteNotificationProcessor(clock);
        var workflow = new QuoteNotificationWorkflow(processedEventRepository, outboxRepository, processor, clock, new ObjectMapper().findAndRegisterModules());
        var event = new QuoteDocumentGeneratedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "https://documents.example/quotes/22222222-2222-2222-2222-222222222222",
                Instant.parse("2026-07-14T10:10:00Z"));

        workflow.process(event);

        assertThat(processedEventRepository.processedEventIds).containsExactly(event.eventId());
        assertThat(outboxRepository.events).hasSize(1);
        assertThat(outboxRepository.events.getFirst().eventType()).isEqualTo("QuoteNotificationSent");
        assertThat(outboxRepository.events.getFirst().payloadJson()).contains("email");
    }

    @Test
    void shouldSkipDuplicateEventWithoutWritingNewOutboxRecord() {
        var clock = Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC);
        var processedEventRepository = new InMemoryProcessedEventRepository();
        var outboxRepository = new InMemoryOutboxEventRepository();
        var processor = new QuoteNotificationProcessor(clock);
        var workflow = new QuoteNotificationWorkflow(processedEventRepository, outboxRepository, processor, clock, new ObjectMapper().findAndRegisterModules());
        var event = new QuoteDocumentGeneratedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "https://documents.example/quotes/22222222-2222-2222-2222-222222222222",
                Instant.parse("2026-07-14T10:10:00Z"));
        processedEventRepository.processedEventIds.add(event.eventId());

        workflow.process(event);

        assertThat(outboxRepository.events).isEmpty();
    }

    private static final class InMemoryProcessedEventRepository implements ProcessedEventRepository {
        private final List<UUID> processedEventIds = new ArrayList<>();

        @Override
        public boolean exists(UUID eventId) {
            return processedEventIds.contains(eventId);
        }

        @Override
        public void save(UUID eventId, Instant processedAt) {
            processedEventIds.add(eventId);
        }
    }

    private static final class InMemoryOutboxEventRepository implements OutboxEventRepository {
        private final List<OutboxEventRecord> events = new ArrayList<>();

        @Override
        public void save(OutboxEventRecord event) {
            events.add(event);
        }

        @Override
        public List<OutboxEventRecord> findUnpublished(int limit) {
            return List.of();
        }

        @Override
        public void markPublished(List<UUID> eventIds, Instant publishedAt) {
        }
    }
}
