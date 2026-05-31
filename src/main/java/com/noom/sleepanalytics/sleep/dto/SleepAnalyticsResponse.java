package com.noom.sleepanalytics.sleep.dto;

public record SleepAnalyticsResponse(
    String userId,
    int daysTracked,
    String averageSleepDuration,
    String averageBedtime,
    String averageWakeTime
) {
}
