package com.rahul.quoteplatform.pricingservice.application;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedEventRepository {

    boolean exists(UUID eventId);

    void save(UUID eventId, Instant processedAt);
}
