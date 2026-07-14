package com.rahul.quoteplatform.quoteapi.application;

import java.util.Optional;
import java.util.UUID;

public interface QuoteRepository {

    void save(QuoteRecord quote);

    default Optional<QuoteRecord> findById(UUID quoteId) {
        return Optional.empty();
    }
}
