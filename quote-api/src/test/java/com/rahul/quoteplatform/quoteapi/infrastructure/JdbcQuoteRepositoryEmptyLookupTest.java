package com.rahul.quoteplatform.quoteapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcQuoteRepositoryEmptyLookupTest {

    private JdbcQuoteRepository repository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:quote-api-quotes-empty;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new JdbcQuoteRepository(jdbcTemplate);
        jdbcTemplate.execute("drop table if exists quotes");
        jdbcTemplate.execute("""
                create table quotes (
                    quote_id uuid primary key,
                    customer_id varchar(100) not null,
                    customer_email varchar(255) not null,
                    requested_amount numeric(19,2) not null,
                    created_at timestamp not null
                )
                """);
    }

    @Test
    void shouldReturnEmptyWhenQuoteDoesNotExist() {
        assertThat(repository.findById(java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"))).isEmpty();
    }
}
