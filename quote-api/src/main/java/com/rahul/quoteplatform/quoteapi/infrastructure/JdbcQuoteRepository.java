package com.rahul.quoteplatform.quoteapi.infrastructure;

import com.rahul.quoteplatform.quoteapi.application.QuoteRecord;
import com.rahul.quoteplatform.quoteapi.application.QuoteRepository;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcQuoteRepository implements QuoteRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcQuoteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(QuoteRecord quote) {
        jdbcTemplate.update("""
                insert into quotes (quote_id, customer_id, customer_email, requested_amount, created_at)
                values (?, ?, ?, ?, ?)
                """,
                quote.quoteId(),
                quote.customerId(),
                quote.customerEmail(),
                quote.requestedAmount(),
                Timestamp.from(quote.createdAt()));
    }

    @Override
    public Optional<QuoteRecord> findById(UUID quoteId) {
        return jdbcTemplate.query("""
                        select quote_id, customer_id, customer_email, requested_amount, created_at
                        from quotes
                        where quote_id = ?
                        """,
                rs -> rs.next()
                        ? Optional.of(new QuoteRecord(
                                rs.getObject("quote_id", UUID.class),
                                rs.getString("customer_id"),
                                rs.getString("customer_email"),
                                rs.getBigDecimal("requested_amount"),
                                rs.getTimestamp("created_at").toInstant()))
                        : Optional.empty(),
                quoteId);
    }
}
