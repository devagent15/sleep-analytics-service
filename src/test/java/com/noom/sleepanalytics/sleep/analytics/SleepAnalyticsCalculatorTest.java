package com.noom.sleepanalytics.sleep.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.noom.sleepanalytics.sleep.SleepLogEntity;
import com.noom.sleepanalytics.sleep.dto.SleepAnalyticsResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SleepAnalyticsCalculatorTest {

    private final SleepAnalyticsCalculator calculator = new SleepAnalyticsCalculator();

    @Test
    void shouldReturnZeroedAnalyticsWhenNoSleepLogsExist() {
        SleepAnalyticsResponse response = calculator.calculate("user_123", List.of());

        assertEquals(0, response.daysTracked());
        assertEquals("00:00:00", response.averageSleepDuration());
        assertEquals("00:00:00", response.averageBedtime());
        assertEquals("00:00:00", response.averageWakeTime());
    }

    @Test
    void shouldCalculateAnalyticsAcrossMidnightBedtimes() {
        SleepLogEntity first = sleepLog("user_123", LocalDate.of(2026, 5, 24), "22:30:00", "08:30:00", true);
        SleepLogEntity second = sleepLog("user_123", LocalDate.of(2026, 5, 25), "01:00:00", "07:00:00", false);

        SleepAnalyticsResponse response = calculator.calculate("user_123", List.of(first, second));

        assertEquals(2, response.daysTracked());
        assertEquals("08:00:00", response.averageSleepDuration());
        assertEquals("23:45:00", response.averageBedtime());
        assertEquals("07:45:00", response.averageWakeTime());
    }

    private SleepLogEntity sleepLog(
        String userId,
        LocalDate wakeUpDate,
        String bedtime,
        String wakeTime,
        boolean isBedtimeBeforeMidnight
    ) {
        SleepLogEntity entity = new SleepLogEntity();
        entity.setUserId(userId);
        entity.setWakeUpDate(wakeUpDate);
        entity.setBedtime(LocalTime.parse(bedtime));
        entity.setWakeTime(LocalTime.parse(wakeTime));
        entity.setBedtimeBeforeMidnight(isBedtimeBeforeMidnight);
        return entity;
    }
}
