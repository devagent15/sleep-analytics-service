package com.noom.sleepanalytics.sleep.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noom.sleepanalytics.sleep.MorningFeeling;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record SleepLogResponse(
    Long id,
    String userId,
    LocalDate wakeUpDate,
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime bedtime,
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime wakeTime,
    boolean isBedtimeBeforeMidnight,
    MorningFeeling morningFeeling,
    String totalTimeInBed,
    Instant createdAt
) {
}
