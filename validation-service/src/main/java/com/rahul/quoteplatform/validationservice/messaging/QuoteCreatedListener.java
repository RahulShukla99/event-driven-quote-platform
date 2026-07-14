package com.rahul.quoteplatform.validationservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteCreatedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.validationservice.application.QuoteValidationWorkflow;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QuoteCreatedListener {

    private static final String QUOTE_CREATED_TOPIC = "quote.created";

    private final ObjectMapper objectMapper;
    private final RetryExecutor retryExecutor;
    private final QuoteValidationWorkflow quoteValidationWorkflow;
    private final DeadLetterPublisher deadLetterPublisher;
    private final int maxAttempts;

    public QuoteCreatedListener(ObjectMapper objectMapper,
                                RetryExecutor retryExecutor,
                                QuoteValidationWorkflow quoteValidationWorkflow,
                                DeadLetterPublisher deadLetterPublisher,
                                @Value("${validation.consumer.retry.max-attempts:3}") int maxAttempts) {
        this.objectMapper = objectMapper;
        this.retryExecutor = retryExecutor;
        this.quoteValidationWorkflow = quoteValidationWorkflow;
        this.deadLetterPublisher = deadLetterPublisher;
        this.maxAttempts = maxAttempts;
    }

    @KafkaListener(topics = QUOTE_CREATED_TOPIC)
    public void listen(String payload) {
        handle(payload);
    }

    public void handle(String payload) {
        try {
            var createdEvent = objectMapper.readValue(payload, QuoteCreatedEvent.class);
            retryExecutor.execute(() -> {
                quoteValidationWorkflow.process(createdEvent);
                return null;
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to process quote created event", exception);
        } catch (RuntimeException exception) {
            deadLetterPublisher.publish(QUOTE_CREATED_TOPIC, payload, exception, maxAttempts);
        }
    }
}
