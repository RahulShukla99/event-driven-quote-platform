package com.rahul.quoteplatform.quoteapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.rahul.quoteplatform.quoteapi.application.OutboxEventRecord;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcOutboxEventRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private JdbcOutboxEventRepository repository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:quote-api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new JdbcOutboxEventRepository(jdbcTemplate);
        jdbcTemplate.execute("drop table if exists outbox_events");
        jdbcTemplate.execute("""
                create table outbox_events (
                    id uuid primary key,
                    aggregate_id uuid not null,
                    aggregate_type varchar(100) not null,
                    event_type varchar(100) not null,
                    payload_json clob not null,
                    created_at timestamp not null,
                    published_at timestamp null
                )
                """);
    }

    @Test
    void shouldSaveAndFindUnpublishedOutboxEvents() {
        var event = new OutboxEventRecord(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Quote",
                "QuoteCreated",
                "{\"customerId\":\"cust-123\"}",
                Instant.parse("2026-07-14T10:15:30Z"),
                null);

        repository.save(event);

        var unpublished = repository.findUnpublished(10);

        assertThat(unpublished).containsExactly(event);
    }

    @Test
    void shouldMarkEventsAsPublished() {
        var eventId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        repository.save(new OutboxEventRecord(
                eventId,
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Quote",
                "QuoteCreated",
                "{\"customerId\":\"cust-123\"}",
                Instant.parse("2026-07-14T10:15:30Z"),
                null));

        repository.markPublished(List.of(eventId), Instant.parse("2026-07-14T10:16:00Z"));

        assertThat(repository.findUnpublished(10)).isEmpty();
    }
}
