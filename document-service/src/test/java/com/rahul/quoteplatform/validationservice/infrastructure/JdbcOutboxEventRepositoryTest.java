package com.rahul.quoteplatform.documentservice.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.rahul.quoteplatform.documentservice.application.OutboxEventRecord;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcOutboxEventRepositoryTest {

    private JdbcOutboxEventRepository repository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:validation-outbox;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new JdbcOutboxEventRepository(jdbcTemplate);
        jdbcTemplate.execute("drop table if exists document_outbox_events");
        jdbcTemplate.execute("""
                create table document_outbox_events (
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
    void shouldSaveFindAndMarkPublishedEvents() {
        var eventId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var event = new OutboxEventRecord(eventId, UUID.fromString("22222222-2222-2222-2222-222222222222"), "Quote", "QuoteValidated", "{}", Instant.parse("2026-07-14T10:15:30Z"), null);

        repository.save(event);

        assertThat(repository.findUnpublished(10)).containsExactly(event);
        repository.markPublished(List.of(eventId), Instant.parse("2026-07-14T10:16:00Z"));
        assertThat(repository.findUnpublished(10)).isEmpty();
    }
}
