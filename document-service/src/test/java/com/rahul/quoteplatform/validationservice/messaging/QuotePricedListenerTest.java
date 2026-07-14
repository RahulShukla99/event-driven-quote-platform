package com.rahul.quoteplatform.documentservice.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuotePricedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.documentservice.application.QuoteDocumentWorkflow;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class QuotePricedListenerTest {

    @Test
    void shouldDelegateValidPayloadToWorkflowWithoutDlq() throws Exception {
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var retryExecutor = new RetryExecutor(3, Duration.ofMillis(1), duration -> { });
        var workflow = mock(QuoteDocumentWorkflow.class);
        var deadLetterPublisher = mock(DeadLetterPublisher.class);
        var listener = new QuotePricedListener(objectMapper, retryExecutor, workflow, deadLetterPublisher, 3);
        var payload = objectMapper.writeValueAsString(new QuotePricedEvent(
                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"),
                java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "customer@example.com",
                new BigDecimal("100.00"),
                new BigDecimal("110.00"),
                Instant.parse("2026-07-14T10:00:00Z")));

        listener.handle(payload);

        verify(workflow).process(any());
        verify(deadLetterPublisher, never()).publish(any(), any(), any(), anyInt());
    }
}
