alter table sleep_logs
    add column morning_feeling varchar(16) not null default 'OK';

alter table sleep_logs
    add constraint chk_sleep_logs_morning_feeling
    check (morning_feeling in ('BAD', 'OK', 'GOOD'));
