create table sleep_logs (
    id bigserial primary key,
    user_id varchar(255) not null,
    wake_up_date date not null,
    bedtime time not null,
    wake_time time not null,
    is_bedtime_before_midnight boolean not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_sleep_logs_user_wake_up_date_desc
    on sleep_logs (user_id, wake_up_date desc);
