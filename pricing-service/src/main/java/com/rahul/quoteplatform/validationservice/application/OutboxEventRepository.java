package com.rahul.quoteplatform.pricingservice.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {

    void save(OutboxEventRecord event);

    List<OutboxEventRecord> findUnpublished(int limit);

    void markPublished(List<UUID> eventIds, Instant publishedAt);
}
