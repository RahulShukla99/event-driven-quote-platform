package com.rahul.quoteplatform.common.retry;

import java.time.Duration;
import java.util.function.Consumer;

public class RetryExecutor {

    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Consumer<Duration> sleeper;

    public RetryExecutor(int maxAttempts, Duration initialBackoff, Consumer<Duration> sleeper) {
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
        this.sleeper = sleeper;
    }

    public <T> T execute(ThrowingSupplier<T> operation) {
        Duration backoff = initialBackoff;
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (RuntimeException exception) {
                lastFailure = exception;
                if (attempt == maxAttempts) {
                    throw exception;
                }
                sleeper.accept(backoff);
                backoff = backoff.multipliedBy(2);
            }
        }
        throw lastFailure;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get();
    }
}
