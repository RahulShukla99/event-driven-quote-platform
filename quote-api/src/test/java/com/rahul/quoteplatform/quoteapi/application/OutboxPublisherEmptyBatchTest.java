package com.rahul.quoteplatform.quoteapi.application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class OutboxPublisherEmptyBatchTest {

    @Test
    void shouldDoNothingWhenNoOutboxEventsArePending() {
        var outboxRepository = new OutboxEventRepository() {
            @Override
            public void save(OutboxEventRecord event) {
            }

            @Override
            public List<OutboxEventRecord> findUnpublished(int limit) {
                return List.of();
            }
        };
        var kafkaTemplate = mock(KafkaTemplate.class);
        var publisher = new OutboxPublisher(outboxRepository, kafkaTemplate, Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC));

        publisher.publishPendingEvents();

        verifyNoInteractions(kafkaTemplate);
    }
}
