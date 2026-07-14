package com.rahul.quoteplatform.quoteapi.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class OutboxPublisherUnsupportedEventTest {

    @Test
    void shouldRejectUnsupportedEventTypes() {
        var outboxRepository = new OutboxEventRepository() {
            @Override
            public void save(OutboxEventRecord event) {
            }

            @Override
            public List<OutboxEventRecord> findUnpublished(int limit) {
                return List.of(new OutboxEventRecord(
                        UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        "Quote",
                        "SomethingElse",
                        "{}",
                        Instant.parse("2026-07-14T10:15:30Z"),
                        null));
            }
        };
        var publisher = new OutboxPublisher(outboxRepository, mock(KafkaTemplate.class), Clock.fixed(Instant.parse("2026-07-14T10:15:30Z"), ZoneOffset.UTC));

        assertThatThrownBy(publisher::publishPendingEvents)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported outbox event type");
    }
}
