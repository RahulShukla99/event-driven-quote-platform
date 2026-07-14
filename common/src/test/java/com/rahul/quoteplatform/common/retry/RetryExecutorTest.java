package com.rahul.quoteplatform.common.retry;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryExecutorTest {

    @Test
    void shouldRetryWithExponentialBackoffUntilSuccess() {
        var sleeps = new ArrayList<Duration>();
        var executor = new RetryExecutor(3, Duration.ofMillis(100), sleeps::add);
        var attempts = new AtomicInteger();

        var result = executor.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("transient");
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(attempts).hasValue(3);
        assertThat(sleeps).containsExactly(Duration.ofMillis(100), Duration.ofMillis(200));
    }

    @Test
    void shouldThrowAfterMaxAttemptsAreExhausted() {
        var executor = new RetryExecutor(3, Duration.ofMillis(100), duration -> { });
        var attempts = new AtomicInteger();

        assertThatThrownBy(() -> executor.execute(() -> {
            attempts.incrementAndGet();
            throw new IllegalStateException("transient");
        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("transient");

        assertThat(attempts).hasValue(3);
    }
}
