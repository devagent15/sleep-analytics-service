package com.noom.sleepanalytics.sleep.dto;

public record SleepLogUpsertResult(
    SleepLogResponse sleepLog,
    boolean created
) {
}
