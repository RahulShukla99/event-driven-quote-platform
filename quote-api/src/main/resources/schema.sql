create table if not exists quotes (
    quote_id uuid primary key,
    customer_id varchar(100) not null,
    customer_email varchar(255) not null,
    requested_amount numeric(19,2) not null,
    created_at timestamp not null
);

create table if not exists outbox_events (
    id uuid primary key,
    aggregate_id uuid not null,
    aggregate_type varchar(100) not null,
    event_type varchar(100) not null,
    payload_json clob not null,
    created_at timestamp not null,
    published_at timestamp null
);
