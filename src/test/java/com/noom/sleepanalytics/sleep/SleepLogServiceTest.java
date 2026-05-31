package com.noom.sleepanalytics.sleep;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.noom.sleepanalytics.common.exception.NotFoundException;
import com.noom.sleepanalytics.sleep.analytics.SleepAnalyticsCalculator;
import com.noom.sleepanalytics.sleep.dto.CreateSleepLogRequest;
import com.noom.sleepanalytics.sleep.dto.SleepAnalyticsResponse;
import com.noom.sleepanalytics.sleep.dto.SleepLogResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SleepLogServiceTest {

    @Mock
    private SleepLogRepository sleepLogRepository;

    @Mock
    private SleepAnalyticsCalculator sleepAnalyticsCalculator;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-30T00:00:00Z"), ZoneOffset.UTC);

    private SleepLogService sleepLogService;

    @BeforeEach
    void setUp() {
        sleepLogService = new SleepLogService(sleepLogRepository, sleepAnalyticsCalculator, clock);
    }

    @Test
    void shouldCreateSleepLog() {
        CreateSleepLogRequest request = new CreateSleepLogRequest(
            "user_123",
            LocalDate.of(2026, 5, 24),
            LocalTime.parse("22:30:00"),
            LocalTime.parse("08:30:00"),
            true
        );

        SleepLogEntity savedEntity = new SleepLogEntity();
        savedEntity.setId(1L);
        savedEntity.setUserId("user_123");
        savedEntity.setWakeUpDate(LocalDate.of(2026, 5, 24));
        savedEntity.setBedtime(LocalTime.parse("22:30:00"));
        savedEntity.setWakeTime(LocalTime.parse("08:30:00"));
        savedEntity.setBedtimeBeforeMidnight(true);
        savedEntity.setCreatedAt(Instant.parse("2026-05-30T13:15:00Z"));

        when(sleepLogRepository.save(any(SleepLogEntity.class))).thenReturn(savedEntity);

        SleepLogResponse response = sleepLogService.createSleepLog(request);

        ArgumentCaptor<SleepLogEntity> captor = ArgumentCaptor.forClass(SleepLogEntity.class);
        verify(sleepLogRepository).save(captor.capture());
        assertEquals("user_123", captor.getValue().getUserId());
        assertEquals(1L, response.id());
        assertEquals("user_123", response.userId());
    }

    @Test
    void shouldReturnLatestSleepLog() {
        SleepLogEntity savedEntity = new SleepLogEntity();
        savedEntity.setId(4L);
        savedEntity.setUserId("user_123");
        savedEntity.setWakeUpDate(LocalDate.of(2026, 5, 27));
        savedEntity.setBedtime(LocalTime.parse("00:00:00"));
        savedEntity.setWakeTime(LocalTime.parse("04:30:00"));
        savedEntity.setBedtimeBeforeMidnight(false);
        savedEntity.setCreatedAt(Instant.parse("2026-05-30T13:15:00Z"));

        when(sleepLogRepository.findFirstByUserIdOrderByWakeUpDateDesc("user_123")).thenReturn(Optional.of(savedEntity));

        SleepLogResponse response = sleepLogService.getLatestSleepLog("user_123");

        assertEquals(4L, response.id());
        assertEquals(LocalDate.of(2026, 5, 27), response.wakeUpDate());
    }

    @Test
    void shouldThrowWhenLatestSleepLogDoesNotExist() {
        when(sleepLogRepository.findFirstByUserIdOrderByWakeUpDateDesc("user_123")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sleepLogService.getLatestSleepLog("user_123"));
    }

    @Test
    void shouldReturnAnalyticsForRollingThirtyDayWindow() {
        List<SleepLogEntity> sleepLogs = List.of(new SleepLogEntity());
        SleepAnalyticsResponse analyticsResponse = new SleepAnalyticsResponse(
            "user_123",
            1,
            "08:00:00",
            "23:00:00",
            "07:00:00"
        );
        when(sleepLogRepository.findAllByUserIdAndWakeUpDateBetweenOrderByWakeUpDateAsc(
            "user_123",
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 30)
        )).thenReturn(sleepLogs);
        when(sleepAnalyticsCalculator.calculate("user_123", sleepLogs)).thenReturn(analyticsResponse);

        SleepAnalyticsResponse response = sleepLogService.getAnalytics("user_123");

        assertEquals(1, response.daysTracked());
    }
}
