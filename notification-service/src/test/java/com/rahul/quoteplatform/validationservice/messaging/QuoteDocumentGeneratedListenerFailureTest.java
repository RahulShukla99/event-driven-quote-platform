package com.rahul.quoteplatform.notificationservice.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.event.QuoteDocumentGeneratedEvent;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.notificationservice.application.QuoteNotificationWorkflow;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuoteDocumentGeneratedListenerFailureTest {

    @Test
    void shouldPublishDlqWhenWorkflowKeepsFailing() throws Exception {
        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var retryExecutor = new RetryExecutor(3, Duration.ofMillis(1), duration -> { });
        var workflow = mock(QuoteNotificationWorkflow.class);
        doThrow(new IllegalStateException("db down")).when(workflow).process(any());
        var deadLetterPublisher = mock(DeadLetterPublisher.class);
        var listener = new QuoteDocumentGeneratedListener(objectMapper, retryExecutor, workflow, deadLetterPublisher, 3);
        var payload = objectMapper.writeValueAsString(new QuoteDocumentGeneratedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "https://documents.example/quotes/22222222-2222-2222-2222-222222222222",
                Instant.parse("2026-07-14T10:10:00Z")));

        listener.handle(payload);

        verify(deadLetterPublisher).publish(eq("quote.document.generated"), eq(payload), argThat(exception -> exception.getMessage().contains("db down")), eq(3));
    }
}
