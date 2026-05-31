package com.noom.sleepanalytics.sleep.analytics;

import com.noom.sleepanalytics.sleep.SleepLogEntity;
import com.noom.sleepanalytics.sleep.dto.SleepAnalyticsResponse;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SleepAnalyticsCalculator {

    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public SleepAnalyticsResponse calculate(String userId, List<SleepLogEntity> sleepLogs) {
        if (sleepLogs.isEmpty()) {
            return new SleepAnalyticsResponse(userId, 0, "00:00:00", "00:00:00", "00:00:00");
        }

        double averageBedtimeMinutes = sleepLogs.stream()
            .mapToInt(this::normalizeBedtimeMinutes)
            .average()
            .orElse(0);

        double averageWakeMinutes = sleepLogs.stream()
            .mapToInt(this::normalizeWakeMinutes)
            .average()
            .orElse(0);

        double averageSleepDurationMinutes = sleepLogs.stream()
            .mapToInt(this::sleepDurationMinutes)
            .average()
            .orElse(0);

        return new SleepAnalyticsResponse(
            userId,
            sleepLogs.size(),
            formatMinutes(averageSleepDurationMinutes),
            formatClockMinutes(averageBedtimeMinutes),
            formatClockMinutes(averageWakeMinutes)
        );
    }

    int normalizeBedtimeMinutes(SleepLogEntity sleepLog) {
        int bedtimeMinutes = toMinutes(sleepLog.getBedtime());
        return sleepLog.isBedtimeBeforeMidnight() ? bedtimeMinutes - MINUTES_PER_DAY : bedtimeMinutes;
    }

    int normalizeWakeMinutes(SleepLogEntity sleepLog) {
        return toMinutes(sleepLog.getWakeTime());
    }

    int sleepDurationMinutes(SleepLogEntity sleepLog) {
        return normalizeWakeMinutes(sleepLog) - normalizeBedtimeMinutes(sleepLog);
    }

    String formatMinutes(double totalMinutes) {
        long roundedMinutes = Math.round(totalMinutes);
        long hours = roundedMinutes / 60;
        long minutes = roundedMinutes % 60;
        return String.format("%02d:%02d:00", hours, minutes);
    }

    String formatClockMinutes(double normalizedMinutes) {
        int roundedMinutes = (int) Math.round(normalizedMinutes);
        int dayWrappedMinutes = Math.floorMod(roundedMinutes, MINUTES_PER_DAY);
        return LocalTime.of(dayWrappedMinutes / 60, dayWrappedMinutes % 60).format(TIME_FORMATTER);
    }

    private int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }
}
