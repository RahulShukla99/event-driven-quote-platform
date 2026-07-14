package com.rahul.quoteplatform.pricingservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteValidatedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.pricingservice.application.QuotePricingWorkflow;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QuoteValidatedListener {

    private static final String SOURCE_TOPIC = "quote.validated";

    private final ObjectMapper objectMapper;
    private final RetryExecutor retryExecutor;
    private final QuotePricingWorkflow quotePricingWorkflow;
    private final DeadLetterPublisher deadLetterPublisher;
    private final int maxAttempts;

    public QuoteValidatedListener(ObjectMapper objectMapper,
                                  RetryExecutor retryExecutor,
                                  QuotePricingWorkflow quotePricingWorkflow,
                                  DeadLetterPublisher deadLetterPublisher,
                                  @Value("${pricing.consumer.retry.max-attempts:3}") int maxAttempts) {
        this.objectMapper = objectMapper;
        this.retryExecutor = retryExecutor;
        this.quotePricingWorkflow = quotePricingWorkflow;
        this.deadLetterPublisher = deadLetterPublisher;
        this.maxAttempts = maxAttempts;
    }

    @KafkaListener(topics = SOURCE_TOPIC)
    public void listen(String payload) {
        handle(payload);
    }

    public void handle(String payload) {
        try {
            var event = objectMapper.readValue(payload, QuoteValidatedEvent.class);
            retryExecutor.execute(() -> {
                quotePricingWorkflow.process(event);
                return null;
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to process quote validated event", exception);
        } catch (RuntimeException exception) {
            deadLetterPublisher.publish(SOURCE_TOPIC, payload, exception, maxAttempts);
        }
    }
}
