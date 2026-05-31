package com.noom.sleepanalytics.sleep.dto;

import java.util.Map;

public record SleepAnalyticsResponse(
    String userId,
    int daysTracked,
    TimeWindowDto range,
    String averageSleepDuration,
    String averageBedtime,
    String averageWakeTime,
    Map<String, Long> morningFeelingFrequencies
) {
}
