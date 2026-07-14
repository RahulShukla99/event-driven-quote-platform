package com.rahul.quoteplatform.validationservice.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteCreatedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.validationservice.application.QuoteValidationWorkflow;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class QuoteCreatedListenerRejectedTest {

    @Test
    void shouldNotThrowWhenWorkflowFailsAndDlqIsPublished() throws Exception {
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var retryExecutor = new RetryExecutor(3, Duration.ofMillis(1), duration -> { });
        var workflow = mock(QuoteValidationWorkflow.class);
        doThrow(new IllegalStateException("db down")).when(workflow).process(any());
        var deadLetterPublisher = mock(DeadLetterPublisher.class);
        var listener = new QuoteCreatedListener(objectMapper, retryExecutor, workflow, deadLetterPublisher, 3);
        var payload = objectMapper.writeValueAsString(new QuoteCreatedEvent(
                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"),
                java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "customer@example.com",
                new BigDecimal("125.50"),
                Instant.parse("2026-07-14T10:00:00Z")));

        listener.handle(payload);

        org.mockito.Mockito.verify(deadLetterPublisher).publish(org.mockito.ArgumentMatchers.eq("quote.created"), org.mockito.ArgumentMatchers.eq(payload), org.mockito.ArgumentMatchers.argThat(exception -> exception.getMessage().contains("db down")), org.mockito.ArgumentMatchers.eq(3));
    }
}
