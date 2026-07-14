package com.rahul.quoteplatform.documentservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteDocumentGeneratedEvent;
import com.rahul.quoteplatform.common.event.QuotePricedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteDocumentWorkflow {

    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final QuoteDocumentProcessor quoteDocumentProcessor;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public QuoteDocumentWorkflow(ProcessedEventRepository processedEventRepository,
                                 OutboxEventRepository outboxEventRepository,
                                 QuoteDocumentProcessor quoteDocumentProcessor,
                                 Clock clock,
                                 ObjectMapper objectMapper) {
        this.processedEventRepository = processedEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.quoteDocumentProcessor = quoteDocumentProcessor;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void process(QuotePricedEvent event) {
        if (processedEventRepository.exists(event.eventId())) {
            return;
        }
        var now = Instant.now(clock);
        var result = quoteDocumentProcessor.generate(event);
        processedEventRepository.save(event.eventId(), now);
        outboxEventRepository.save(new OutboxEventRecord(
                UUID.randomUUID(),
                event.quoteId(),
                "Quote",
                "QuoteDocumentGenerated",
                toJson(result),
                now,
                null));
    }

    private String toJson(QuoteDocumentGeneratedEvent result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize document result", exception);
        }
    }
}
