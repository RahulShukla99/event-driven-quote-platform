package com.rahul.quoteplatform.validationservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteCreatedEvent;
import com.rahul.quoteplatform.common.event.QuoteRejectedEvent;
import com.rahul.quoteplatform.common.event.QuoteValidatedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteValidationWorkflow {

    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final QuoteValidationProcessor quoteValidationProcessor;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public QuoteValidationWorkflow(ProcessedEventRepository processedEventRepository,
                                   OutboxEventRepository outboxEventRepository,
                                   QuoteValidationProcessor quoteValidationProcessor,
                                   Clock clock,
                                   ObjectMapper objectMapper) {
        this.processedEventRepository = processedEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.quoteValidationProcessor = quoteValidationProcessor;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void process(QuoteCreatedEvent event) {
        if (processedEventRepository.exists(event.eventId())) {
            return;
        }
        var now = Instant.now(clock);
        var result = quoteValidationProcessor.validate(event);
        processedEventRepository.save(event.eventId(), now);
        outboxEventRepository.save(new OutboxEventRecord(
                UUID.randomUUID(),
                event.quoteId(),
                "Quote",
                eventType(result),
                toJson(result),
                now,
                null));
    }

    private String eventType(Object result) {
        if (result instanceof QuoteValidatedEvent) {
            return "QuoteValidated";
        }
        if (result instanceof QuoteRejectedEvent) {
            return "QuoteRejected";
        }
        throw new IllegalArgumentException("Unsupported validation result: " + result.getClass().getName());
    }

    private String toJson(Object result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize validation result", exception);
        }
    }
}
