package com.rahul.quoteplatform.notificationservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteDocumentGeneratedEvent;
import com.rahul.quoteplatform.common.event.QuoteNotificationSentEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteNotificationWorkflow {

    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final QuoteNotificationProcessor quoteNotificationProcessor;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public QuoteNotificationWorkflow(ProcessedEventRepository processedEventRepository,
                                     OutboxEventRepository outboxEventRepository,
                                     QuoteNotificationProcessor quoteNotificationProcessor,
                                     Clock clock,
                                     ObjectMapper objectMapper) {
        this.processedEventRepository = processedEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.quoteNotificationProcessor = quoteNotificationProcessor;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void process(QuoteDocumentGeneratedEvent event) {
        if (processedEventRepository.exists(event.eventId())) {
            return;
        }
        var now = Instant.now(clock);
        var result = quoteNotificationProcessor.send(event);
        processedEventRepository.save(event.eventId(), now);
        outboxEventRepository.save(new OutboxEventRecord(
                UUID.randomUUID(),
                event.quoteId(),
                "Quote",
                "QuoteNotificationSent",
                toJson(result),
                now,
                null));
    }

    private String toJson(QuoteNotificationSentEvent result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize notification result", exception);
        }
    }
}
