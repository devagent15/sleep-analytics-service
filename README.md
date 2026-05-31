# sleep-analytics-service

This project is a Spring Boot service for storing nightly sleep logs and returning simple sleep analytics for the last 30 days.

The API supports three workflows:
- create a sleep log for a user
- fetch the latest sleep log for a user
- fetch 30-day average sleep analytics for a user

PostgreSQL is used for persistence, and Flyway manages the database schema.

## Project Overview

Each sleep log stores:
- `userId`
- `wakeUpDate`
- `bedtime`
- `wakeTime`
- `isBedtimeBeforeMidnight`
- `morningFeeling`

The service is intentionally user-aware without implementing authentication or authorization. Every request is scoped by `userId`.

One intentional product rule from the assignment is enforced in the create endpoint: new sleep logs may only be created for the current date, so the API matches the idea of "last night" rather than allowing arbitrary historical writes through that endpoint.

## How To Run

The easiest way to run the project is with Docker:

```bash
docker compose up --build
```

This starts:
- PostgreSQL on `localhost:5432`
- the API on `localhost:8080`

To stop everything:

```bash
docker compose down
```

If you make code changes and want a clean rebuild:

```bash
docker compose down
docker compose up --build
```

## How To Test The API

The endpoints are:
- `POST /api/v1/sleep-logs`
- `GET /api/v1/sleep-logs/latest?userId=...`
- `GET /api/v1/sleep-logs/analytics/averages?userId=...`

I also included a Postman collection in [sleep-api.postman_collection.json](/Users/shrishauday/PycharmProjects/sleep-analytics-service/sleep-api.postman_collection.json).

### 1. Create a sleep log

```bash
curl -X POST http://localhost:8080/api/v1/sleep-logs \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_123",
    "wakeUpDate": "2026-05-31",
    "bedtime": "22:30:00",
    "wakeTime": "08:30:00",
    "isBedtimeBeforeMidnight": true,
    "morningFeeling": "GOOD"
  }'
```

Expected response:

```json
{
  "id": 1,
  "userId": "user_123",
  "wakeUpDate": "2026-05-31",
  "bedtime": "22:30:00",
  "wakeTime": "08:30:00",
  "isBedtimeBeforeMidnight": true,
  "morningFeeling": "GOOD",
  "totalTimeInBed": "10:00:00",
  "createdAt": "2026-05-31T12:00:00Z"
}
```

Notes:
- `wakeUpDate` must be today's date
- valid `morningFeeling` values are `BAD`, `OK`, and `GOOD`
- `POST /api/v1/sleep-logs` behaves as an upsert for `userId + wakeUpDate`
- the first request for a given user and date returns `201 Created`
- posting the same user and date again updates that night’s log and returns `200 OK`

### 2. Fetch the latest sleep log

```bash
curl "http://localhost:8080/api/v1/sleep-logs/latest?userId=user_123"
```

Expected behavior:
- returns the sleep log with the greatest `wakeUpDate` for that user
- returns `404` if the user has no sleep logs
- includes both `morningFeeling` and derived `totalTimeInBed`

### 3. Fetch 30-day analytics

```bash
curl "http://localhost:8080/api/v1/sleep-logs/analytics/averages?userId=user_123"
```

Expected response shape:

```json
{
  "userId": "user_123",
  "daysTracked": 29,
  "range": {
    "startDate": "2026-05-02",
    "endDate": "2026-05-31"
  },
  "averageSleepDuration": "06:48:00",
  "averageBedtime": "23:41:00",
  "averageWakeTime": "06:28:00",
  "morningFeelingFrequencies": {
    "BAD": 4,
    "OK": 9,
    "GOOD": 16
  }
}
```

Expected behavior:
- the query window is `current_date - 29` through `current_date`
- if there are no records in that window, the service returns `200 OK` with:
  - `daysTracked = 0`
  - zeroed time values
  - a valid `range`
  - zero counts for `BAD`, `OK`, and `GOOD`

## Calculation Logic

The part of the assignment that needed the most care was averaging bedtime correctly.

The problem is that bedtimes cross midnight. For example:
- `23:30` and `00:30` are only one hour apart in real life
- but if you average them naively as plain clock values, the result looks wrong

To handle that, the service stores the raw bedtime and wake time along with `isBedtimeBeforeMidnight`.

The assignment also asks for the user’s morning feeling. That value is treated as user-provided input rather than something inferred from sleep duration. The service validates it as one of:
- `BAD`
- `OK`
- `GOOD`

The 30-day analytics response then returns simple frequency counts for those values across the same date window used for the averages.

The analytics calculator uses this rule:
- if `isBedtimeBeforeMidnight = true`, bedtime is treated as a time on the previous evening
- if `isBedtimeBeforeMidnight = false`, bedtime is treated as a time after midnight on the same morning window

Internally, bedtime is normalized into minutes relative to midnight:
- `23:30` with `true` becomes a negative offset relative to wake day
- `00:30` with `false` stays on the same-day side of midnight

Sleep duration is then computed as:

```text
sleepDuration = normalizedWakeTime - normalizedBedtime
```

After averaging:
- average sleep duration is formatted as `HH:mm:ss`
- average bedtime is wrapped back onto a 24-hour clock
- average wake time is wrapped back onto a 24-hour clock

This keeps the averages mathematically consistent for mixed data such as:
- some nights with bedtimes before midnight
- some nights with bedtimes after midnight

## Database Notes

The schema is created by Flyway in:

- [V1__create_sleep_logs.sql](/Users/shrishauday/PycharmProjects/sleep-analytics-service/src/main/resources/db/migration/V1__create_sleep_logs.sql)
- [V2__add_morning_feeling.sql](/Users/shrishauday/PycharmProjects/sleep-analytics-service/src/main/resources/db/migration/V2__add_morning_feeling.sql)

The main table is `sleep_logs`.

To inspect the database while the service is running:

```bash
docker compose exec db psql -U sleep -d sleep_analytics
```

Then run:

```sql
select *
from sleep_logs
order by wake_up_date desc, user_id;
```

Useful verification queries:

Check how many rows were loaded per user:

```sql
select user_id, count(*)
from sleep_logs
where user_id in ('user_123', 'user_567')
group by user_id
order by user_id;
```

Check the latest records:

```sql
select id, user_id, wake_up_date, bedtime, wake_time, is_bedtime_before_midnight, morning_feeling
from sleep_logs
where user_id = 'user_123'
order by wake_up_date desc
limit 5;
```

Check the analytics window for one user:

```sql
select count(*), min(wake_up_date), max(wake_up_date)
from sleep_logs
where user_id = 'user_123'
  and wake_up_date between current_date - 29 and current_date;
```

Check morning feeling frequencies:

```sql
select morning_feeling, count(*)
from sleep_logs
where user_id = 'user_123'
  and wake_up_date between current_date - 29 and current_date
group by morning_feeling
order by morning_feeling;
```

## Testing Notes

The project includes:
- controller tests
- service tests
- repository tests
- analytics calculator tests

The test configuration uses H2 in PostgreSQL compatibility mode for repository tests, while the application itself runs against PostgreSQL.

## A Small Browser Note

Opening `http://localhost:8080/api/v1/sleep-logs` in a browser sends a `GET` request.

That route is intentionally `POST` only, so it is not the right way to test log creation. For manual testing:
- use a browser for the `GET` endpoints
- use Postman or `curl` for the `POST` endpoint
