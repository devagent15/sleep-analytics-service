package com.noom.sleepanalytics.sleep.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateSleepLogRequest(
    @NotBlank(message = "userId is required")
    String userId,
    @NotNull(message = "wakeUpDate is required")
    LocalDate wakeUpDate,
    @NotNull(message = "bedtime is required")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime bedtime,
    @NotNull(message = "wakeTime is required")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime wakeTime,
    boolean isBedtimeBeforeMidnight
) {
}
