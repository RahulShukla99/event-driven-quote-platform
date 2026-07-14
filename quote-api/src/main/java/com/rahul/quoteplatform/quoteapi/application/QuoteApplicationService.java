package com.rahul.quoteplatform.quoteapi.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteCreatedEvent;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteApplicationService {

    private final QuoteRepository quoteRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public QuoteApplicationService(QuoteRepository quoteRepository, OutboxEventRepository outboxEventRepository, Clock clock, ObjectMapper objectMapper) {
        this.quoteRepository = quoteRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CreateQuoteResponse createQuote(CreateQuoteCommand command) {
        var quoteId = UUID.randomUUID();
        var now = Instant.now(clock);
        quoteRepository.save(new QuoteRecord(quoteId, command.customerId(), command.customerEmail(), command.requestedAmount(), now));
        var event = new QuoteCreatedEvent(UUID.randomUUID(), quoteId, command.customerId(), command.customerEmail(), command.requestedAmount(), now);
        outboxEventRepository.save(new OutboxEventRecord(
                UUID.randomUUID(),
                quoteId,
                "Quote",
                "QuoteCreated",
                toJson(event),
                now,
                null));
        return new CreateQuoteResponse(quoteId);
    }

    private String toJson(QuoteCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize quote created event", exception);
        }
    }
}
