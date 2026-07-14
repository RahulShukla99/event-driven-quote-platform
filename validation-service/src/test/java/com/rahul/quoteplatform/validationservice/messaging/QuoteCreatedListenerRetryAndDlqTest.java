package com.rahul.quoteplatform.validationservice.messaging;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteCreatedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.validationservice.application.QuoteValidationWorkflow;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

class QuoteCreatedListenerRetryAndDlqTest {

    @Test
    void shouldRetryWorkflowAndPublishDlqAfterExhaustingAttempts() throws Exception {
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var retryExecutor = new RetryExecutor(3, Duration.ofMillis(10), duration -> { });
        var workflow = mock(QuoteValidationWorkflow.class);
        var deadLetterPublisher = mock(DeadLetterPublisher.class);
        var listener = new QuoteCreatedListener(objectMapper, retryExecutor, workflow, deadLetterPublisher, 3);
        var payload = objectMapper.writeValueAsString(new QuoteCreatedEvent(
                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"),
                java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "customer@example.com",
                new BigDecimal("125.50"),
                Instant.parse("2026-07-14T10:00:00Z")));

        doThrow(new IllegalStateException("db down")).when(workflow).process(any());

        listener.handle(payload);

        verify(deadLetterPublisher).publish(eq("quote.created"), eq(payload), argThat(exception -> exception.getMessage().contains("db down")), eq(3));
    }
}
