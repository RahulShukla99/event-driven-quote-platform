package com.rahul.quoteplatform.documentservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuotePricedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.documentservice.application.QuoteDocumentWorkflow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class QuotePricedListener {

    private static final String SOURCE_TOPIC = "quote.priced";

    private final ObjectMapper objectMapper;
    private final RetryExecutor retryExecutor;
    private final QuoteDocumentWorkflow quoteDocumentWorkflow;
    private final DeadLetterPublisher deadLetterPublisher;
    private final int maxAttempts;

    public QuotePricedListener(ObjectMapper objectMapper,
                               RetryExecutor retryExecutor,
                               QuoteDocumentWorkflow quoteDocumentWorkflow,
                               DeadLetterPublisher deadLetterPublisher,
                               @Value("${document.consumer.retry.max-attempts:3}") int maxAttempts) {
        this.objectMapper = objectMapper;
        this.retryExecutor = retryExecutor;
        this.quoteDocumentWorkflow = quoteDocumentWorkflow;
        this.deadLetterPublisher = deadLetterPublisher;
        this.maxAttempts = maxAttempts;
    }

    @KafkaListener(topics = SOURCE_TOPIC)
    public void listen(String payload) {
        handle(payload);
    }

    public void handle(String payload) {
        try {
            var event = objectMapper.readValue(payload, QuotePricedEvent.class);
            retryExecutor.execute(() -> {
                quoteDocumentWorkflow.process(event);
                return null;
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to process quote priced event", exception);
        } catch (RuntimeException exception) {
            deadLetterPublisher.publish(SOURCE_TOPIC, payload, exception, maxAttempts);
        }
    }
}
