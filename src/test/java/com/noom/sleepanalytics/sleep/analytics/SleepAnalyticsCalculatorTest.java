package com.noom.sleepanalytics.sleep.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.noom.sleepanalytics.sleep.MorningFeeling;
import com.noom.sleepanalytics.sleep.SleepLogEntity;
import com.noom.sleepanalytics.sleep.dto.SleepAnalyticsResponse;
import com.noom.sleepanalytics.sleep.dto.TimeWindowDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SleepAnalyticsCalculatorTest {

    private final SleepAnalyticsCalculator calculator = new SleepAnalyticsCalculator();

    @Test
    void shouldReturnZeroedAnalyticsWhenNoSleepLogsExist() {
        SleepAnalyticsResponse response = calculator.calculate(
            "user_123",
            new TimeWindowDto(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 30)),
            List.of()
        );

        assertEquals(0, response.daysTracked());
        assertEquals("00:00:00", response.averageSleepDuration());
        assertEquals("00:00:00", response.averageBedtime());
        assertEquals("00:00:00", response.averageWakeTime());
        assertEquals(0L, response.morningFeelingFrequencies().get("GOOD"));
    }

    @Test
    void shouldCalculateAnalyticsAcrossMidnightBedtimes() {
        SleepLogEntity first = sleepLog("user_123", LocalDate.of(2026, 5, 24), "22:30:00", "08:30:00", true, MorningFeeling.GOOD);
        SleepLogEntity second = sleepLog("user_123", LocalDate.of(2026, 5, 25), "01:00:00", "07:00:00", false, MorningFeeling.BAD);

        SleepAnalyticsResponse response = calculator.calculate(
            "user_123",
            new TimeWindowDto(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 30)),
            List.of(first, second)
        );

        assertEquals(2, response.daysTracked());
        assertEquals("08:00:00", response.averageSleepDuration());
        assertEquals("23:45:00", response.averageBedtime());
        assertEquals("07:45:00", response.averageWakeTime());
        assertEquals(1L, response.morningFeelingFrequencies().get("GOOD"));
        assertEquals(1L, response.morningFeelingFrequencies().get("BAD"));
        assertEquals(LocalDate.of(2026, 5, 1), response.range().startDate());
    }

    private SleepLogEntity sleepLog(
        String userId,
        LocalDate wakeUpDate,
        String bedtime,
        String wakeTime,
        boolean isBedtimeBeforeMidnight,
        MorningFeeling morningFeeling
    ) {
        SleepLogEntity entity = new SleepLogEntity();
        entity.setUserId(userId);
        entity.setWakeUpDate(wakeUpDate);
        entity.setBedtime(LocalTime.parse(bedtime));
        entity.setWakeTime(LocalTime.parse(wakeTime));
        entity.setBedtimeBeforeMidnight(isBedtimeBeforeMidnight);
        entity.setMorningFeeling(morningFeeling);
        return entity;
    }
}
