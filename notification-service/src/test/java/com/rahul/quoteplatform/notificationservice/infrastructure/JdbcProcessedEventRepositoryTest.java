package com.rahul.quoteplatform.notificationservice.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcProcessedEventRepositoryTest {

    private JdbcProcessedEventRepository repository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:validation-processed;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new JdbcProcessedEventRepository(jdbcTemplate);
        jdbcTemplate.execute("drop table if exists notification_processed_events");
        jdbcTemplate.execute("""
                create table notification_processed_events (
                    event_id uuid primary key,
                    processed_at timestamp not null
                )
                """);
    }

    @Test
    void shouldStoreAndDetectProcessedEvent() {
        var eventId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        assertThat(repository.exists(eventId)).isFalse();
        repository.save(eventId, Instant.parse("2026-07-14T10:15:30Z"));

        assertThat(repository.exists(eventId)).isTrue();
    }
}
