package com.rahul.quoteplatform.quoteapi.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {

    private static final String QUOTE_CREATED_TOPIC = "quote.created";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Clock clock;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate, Clock clock) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${quote.outbox.poll-interval-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        var pendingEvents = outboxEventRepository.findUnpublished(100);
        if (pendingEvents.isEmpty()) {
            return;
        }
        var publishedIds = new ArrayList<UUID>();
        for (OutboxEventRecord event : pendingEvents) {
            kafkaTemplate.send(topicFor(event.eventType()), event.payloadJson());
            publishedIds.add(event.id());
        }
        outboxEventRepository.markPublished(publishedIds, Instant.now(clock));
    }

    private String topicFor(String eventType) {
        if ("QuoteCreated".equals(eventType)) {
            return QUOTE_CREATED_TOPIC;
        }
        throw new IllegalArgumentException("Unsupported outbox event type: " + eventType);
    }
}
