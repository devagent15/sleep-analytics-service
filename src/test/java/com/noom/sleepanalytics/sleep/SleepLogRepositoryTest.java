package com.noom.sleepanalytics.sleep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class SleepLogRepositoryTest {

    @Autowired
    private SleepLogRepository sleepLogRepository;

    @Test
    void shouldReturnLatestSleepLogByWakeUpDate() {
        sleepLogRepository.save(sleepLog("user_123", LocalDate.of(2026, 5, 20), "22:30:00", "06:30:00", true));
        sleepLogRepository.save(sleepLog("user_123", LocalDate.of(2026, 5, 24), "23:30:00", "07:30:00", true));

        Optional<SleepLogEntity> latest = sleepLogRepository.findFirstByUserIdOrderByWakeUpDateDesc("user_123");

        assertTrue(latest.isPresent());
        assertEquals(LocalDate.of(2026, 5, 24), latest.get().getWakeUpDate());
    }

    @Test
    void shouldReturnLogsWithinDateRangeInAscendingOrder() {
        sleepLogRepository.save(sleepLog("user_123", LocalDate.of(2026, 5, 5), "23:00:00", "07:00:00", true));
        sleepLogRepository.save(sleepLog("user_123", LocalDate.of(2026, 5, 10), "23:15:00", "07:15:00", true));
        sleepLogRepository.save(sleepLog("user_123", LocalDate.of(2026, 5, 15), "23:30:00", "07:30:00", true));

        List<SleepLogEntity> logs = sleepLogRepository.findAllByUserIdAndWakeUpDateBetweenOrderByWakeUpDateAsc(
            "user_123",
            LocalDate.of(2026, 5, 8),
            LocalDate.of(2026, 5, 16)
        );

        assertEquals(2, logs.size());
        assertEquals(LocalDate.of(2026, 5, 10), logs.get(0).getWakeUpDate());
        assertEquals(LocalDate.of(2026, 5, 15), logs.get(1).getWakeUpDate());
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
        entity.setMorningFeeling(MorningFeeling.OK);
        entity.setCreatedAt(Instant.parse("2026-05-30T00:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2026-05-30T00:00:00Z"));
        return entity;
    }
}
