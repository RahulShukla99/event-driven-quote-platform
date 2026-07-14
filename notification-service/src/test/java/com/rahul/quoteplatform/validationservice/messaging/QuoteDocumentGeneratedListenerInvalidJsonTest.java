package com.rahul.quoteplatform.notificationservice.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.notificationservice.application.QuoteNotificationWorkflow;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class QuoteDocumentGeneratedListenerInvalidJsonTest {

    @Test
    void shouldFailFastForMalformedJson() {
        var listener = new QuoteDocumentGeneratedListener(
                new ObjectMapper().findAndRegisterModules(),
                new RetryExecutor(3, Duration.ofMillis(1), duration -> { }),
                mock(QuoteNotificationWorkflow.class),
                mock(DeadLetterPublisher.class),
                3);

        assertThatThrownBy(() -> listener.handle("not-json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to process quote document generated event");
    }
}
