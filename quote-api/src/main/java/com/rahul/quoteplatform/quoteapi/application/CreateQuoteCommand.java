package com.rahul.quoteplatform.quoteapi.application;

import java.math.BigDecimal;

public record CreateQuoteCommand(String customerId, String customerEmail, BigDecimal requestedAmount) {
}
