package com.rahul.quoteplatform.pricingservice.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteValidatedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.pricingservice.application.QuotePricingWorkflow;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class QuoteValidatedListenerRetryAndDlqTest {

    @Test
    void shouldRetryWorkflowAndPublishDlqAfterExhaustingAttempts() throws Exception {
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var retryExecutor = new RetryExecutor(3, Duration.ofMillis(10), duration -> { });
        var workflow = mock(QuotePricingWorkflow.class);
        doThrow(new IllegalStateException("db down")).when(workflow).process(any());
        var deadLetterPublisher = mock(DeadLetterPublisher.class);
        var listener = new QuoteValidatedListener(objectMapper, retryExecutor, workflow, deadLetterPublisher, 3);
        var payload = objectMapper.writeValueAsString(new QuoteValidatedEvent(
                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"),
                java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "customer@example.com",
                new BigDecimal("100.00"),
                Instant.parse("2026-07-14T10:00:00Z")));

        listener.handle(payload);

        verify(deadLetterPublisher).publish(eq("quote.validated"), eq(payload), argThat(exception -> exception.getMessage().contains("db down")), eq(3));
    }
}
