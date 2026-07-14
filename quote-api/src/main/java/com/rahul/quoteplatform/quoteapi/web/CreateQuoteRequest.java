package com.rahul.quoteplatform.quoteapi.web;

import java.math.BigDecimal;

public record CreateQuoteRequest(String customerId, String customerEmail, BigDecimal requestedAmount) {
}
