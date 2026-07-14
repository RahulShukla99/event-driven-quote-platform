package com.rahul.quoteplatform.documentservice.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rahul.quoteplatform.common.retry.RetryExecutor;
import com.rahul.quoteplatform.documentservice.application.QuoteDocumentWorkflow;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class QuotePricedListenerInvalidJsonTest {

    @Test
    void shouldFailFastForMalformedJson() {
        var listener = new QuotePricedListener(
                new ObjectMapper().findAndRegisterModules(),
                new RetryExecutor(3, Duration.ofMillis(1), duration -> { }),
                mock(QuoteDocumentWorkflow.class),
                mock(DeadLetterPublisher.class),
                3);

        assertThatThrownBy(() -> listener.handle("not-json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to process quote priced event");
    }
}
