package com.rahul.quoteplatform.pricingservice.infrastructure;

import com.rahul.quoteplatform.pricingservice.application.ProcessedEventRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcProcessedEventRepository implements ProcessedEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcProcessedEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean exists(UUID eventId) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from pricing_processed_events where event_id = ?", Integer.class, eventId);
        return count != null && count > 0;
    }

    @Override
    public void save(UUID eventId, Instant processedAt) {
        jdbcTemplate.update("insert into pricing_processed_events (event_id, processed_at) values (?, ?)", eventId, Timestamp.from(processedAt));
    }
}
