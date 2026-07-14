package com.rahul.quoteplatform.quoteapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.rahul.quoteplatform.quoteapi.application.QuoteRecord;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcQuoteRepositoryTest {

    private JdbcQuoteRepository repository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:quote-api-quotes;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        repository = new JdbcQuoteRepository(jdbcTemplate);
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
    void shouldSaveQuote() {
        var quote = new QuoteRecord(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "cust-123",
                "customer@example.com",
                new BigDecimal("125.50"),
                Instant.parse("2026-07-14T10:15:30Z"));

        repository.save(quote);

        assertThat(repository.findById(quote.quoteId())).contains(quote);
    }
}
