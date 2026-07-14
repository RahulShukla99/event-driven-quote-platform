package com.rahul.quoteplatform.quoteapi.infrastructure;

import com.rahul.quoteplatform.quoteapi.application.OutboxEventRecord;
import com.rahul.quoteplatform.quoteapi.application.OutboxEventRepository;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcOutboxEventRepository implements OutboxEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcOutboxEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(OutboxEventRecord event) {
        jdbcTemplate.update("""
                insert into outbox_events (id, aggregate_id, aggregate_type, event_type, payload_json, created_at, published_at)
                values (?, ?, ?, ?, ?, ?, ?)
                """,
                event.id(),
                event.aggregateId(),
                event.aggregateType(),
                event.eventType(),
                event.payloadJson(),
                Timestamp.from(event.createdAt()),
                event.publishedAt() == null ? null : Timestamp.from(event.publishedAt()));
    }

    @Override
    public List<OutboxEventRecord> findUnpublished(int limit) {
        return jdbcTemplate.query("""
                        select id, aggregate_id, aggregate_type, event_type, payload_json, created_at, published_at
                        from outbox_events
                        where published_at is null
                        order by created_at, id
                        limit ?
                        """,
                (rs, rowNum) -> new OutboxEventRecord(
                        rs.getObject("id", UUID.class),
                        rs.getObject("aggregate_id", UUID.class),
                        rs.getString("aggregate_type"),
                        rs.getString("event_type"),
                        rs.getString("payload_json"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("published_at") == null ? null : rs.getTimestamp("published_at").toInstant()),
                limit);
    }

    @Override
    public void markPublished(List<UUID> eventIds, java.time.Instant publishedAt) {
        for (UUID eventId : eventIds) {
            jdbcTemplate.update("update outbox_events set published_at = ? where id = ?",
                    Timestamp.from(publishedAt),
                    eventId);
        }
    }
}
