package com.rahul.quoteplatform.quoteapi.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuoteRecord(UUID quoteId, String customerId, String customerEmail, BigDecimal requestedAmount, Instant createdAt) {
}
