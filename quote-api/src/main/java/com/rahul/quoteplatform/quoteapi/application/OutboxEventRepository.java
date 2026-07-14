package com.rahul.quoteplatform.quoteapi.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {

    void save(OutboxEventRecord event);

    default List<OutboxEventRecord> findUnpublished(int limit) {
        return List.of();
    }

    default void markPublished(List<UUID> eventIds, Instant publishedAt) {
    }
}
