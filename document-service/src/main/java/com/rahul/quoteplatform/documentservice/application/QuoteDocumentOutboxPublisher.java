package com.rahul.quoteplatform.documentservice.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QuoteDocumentOutboxPublisher {

    private final com.rahul.quoteplatform.documentservice.application.OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Clock clock;

    public QuoteDocumentOutboxPublisher(OutboxEventRepository outboxEventRepository, KafkaTemplate<String, String> kafkaTemplate, Clock clock) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${document.outbox.poll-interval-ms:1000}")
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
        if ("QuoteDocumentGenerated".equals(eventType)) {
            return "quote.document.generated";
        }
        throw new IllegalArgumentException("Unsupported document event type: " + eventType);
    }
}
