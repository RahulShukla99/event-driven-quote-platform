package com.rahul.quoteplatform.notificationservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteDocumentGeneratedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.notificationservice.application.QuoteNotificationWorkflow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class QuoteDocumentGeneratedListener {

    private static final String SOURCE_TOPIC = "quote.document.generated";

    private final ObjectMapper objectMapper;
    private final RetryExecutor retryExecutor;
    private final QuoteNotificationWorkflow quoteNotificationWorkflow;
    private final DeadLetterPublisher deadLetterPublisher;
    private final int maxAttempts;

    public QuoteDocumentGeneratedListener(ObjectMapper objectMapper,
                                          RetryExecutor retryExecutor,
                                          QuoteNotificationWorkflow quoteNotificationWorkflow,
                                          DeadLetterPublisher deadLetterPublisher,
                                          @Value("${notification.consumer.retry.max-attempts:3}") int maxAttempts) {
        this.objectMapper = objectMapper;
        this.retryExecutor = retryExecutor;
        this.quoteNotificationWorkflow = quoteNotificationWorkflow;
        this.deadLetterPublisher = deadLetterPublisher;
        this.maxAttempts = maxAttempts;
    }

    @KafkaListener(topics = SOURCE_TOPIC)
    public void listen(String payload) {
        handle(payload);
    }

    public void handle(String payload) {
        try {
            var event = objectMapper.readValue(payload, QuoteDocumentGeneratedEvent.class);
            retryExecutor.execute(() -> {
                quoteNotificationWorkflow.process(event);
                return null;
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to process quote document generated event", exception);
        } catch (RuntimeException exception) {
            deadLetterPublisher.publish(SOURCE_TOPIC, payload, exception, maxAttempts);
        }
    }
}
