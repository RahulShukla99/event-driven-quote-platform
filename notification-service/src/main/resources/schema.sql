create table if not exists notification_processed_events (
    event_id uuid primary key,
    processed_at timestamp not null
);

create table if not exists notification_outbox_events (
    id uuid primary key,
    aggregate_id uuid not null,
    aggregate_type varchar(100) not null,
    event_type varchar(100) not null,
    payload_json clob not null,
    created_at timestamp not null,
    published_at timestamp null
);
