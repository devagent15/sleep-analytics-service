delete from sleep_logs
where id in (
    select id
    from (
        select
            id,
            row_number() over (
                partition by user_id, wake_up_date
                order by updated_at desc, created_at desc, id desc
            ) as row_rank
        from sleep_logs
    ) ranked
    where row_rank > 1
);

alter table sleep_logs
    add constraint uk_sleep_logs_user_id_wake_up_date
    unique (user_id, wake_up_date);
