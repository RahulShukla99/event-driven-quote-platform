package com.rahul.quoteplatform.pricingservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuotePricedEvent;
import com.rahul.quoteplatform.common.event.QuoteValidatedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuotePricingWorkflow {

    private final ProcessedEventRepository processedEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final QuotePricingProcessor quotePricingProcessor;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public QuotePricingWorkflow(ProcessedEventRepository processedEventRepository,
                                OutboxEventRepository outboxEventRepository,
                                QuotePricingProcessor quotePricingProcessor,
                                Clock clock,
                                ObjectMapper objectMapper) {
        this.processedEventRepository = processedEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.quotePricingProcessor = quotePricingProcessor;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void process(QuoteValidatedEvent event) {
        if (processedEventRepository.exists(event.eventId())) {
            return;
        }
        var now = Instant.now(clock);
        QuotePricedEvent result = quotePricingProcessor.price(event);
        processedEventRepository.save(event.eventId(), now);
        outboxEventRepository.save(new OutboxEventRecord(
                UUID.randomUUID(),
                event.quoteId(),
                "Quote",
                "QuotePriced",
                toJson(result),
                now,
                null));
    }

    private String toJson(QuotePricedEvent result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize pricing result", exception);
        }
    }
}
