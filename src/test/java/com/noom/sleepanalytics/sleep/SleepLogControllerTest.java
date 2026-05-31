package com.noom.sleepanalytics.sleep;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.noom.sleepanalytics.common.exception.ApiExceptionHandler;
import com.noom.sleepanalytics.common.exception.NotFoundException;
import com.noom.sleepanalytics.sleep.dto.CreateSleepLogRequest;
import com.noom.sleepanalytics.sleep.dto.SleepAnalyticsResponse;
import com.noom.sleepanalytics.sleep.dto.SleepLogResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SleepLogController.class)
@Import({ApiExceptionHandler.class, SleepLogControllerTest.MockConfig.class})
class SleepLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SleepLogService sleepLogService;

    @Test
    void shouldCreateSleepLog() throws Exception {
        SleepLogResponse response = new SleepLogResponse(
            1L,
            "user_123",
            LocalDate.of(2026, 5, 24),
            LocalTime.parse("22:30:00"),
            LocalTime.parse("08:30:00"),
            true,
            Instant.parse("2026-05-30T13:15:00Z")
        );
        when(sleepLogService.createSleepLog(eq(new CreateSleepLogRequest(
            "user_123",
            LocalDate.of(2026, 5, 24),
            LocalTime.parse("22:30:00"),
            LocalTime.parse("08:30:00"),
            true
        )))).thenReturn(response);

        mockMvc.perform(post("/api/v1/sleep-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": "user_123",
                      "wakeUpDate": "2026-05-24",
                      "bedtime": "22:30:00",
                      "wakeTime": "08:30:00",
                      "isBedtimeBeforeMidnight": true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.userId").value("user_123"));
    }

    @Test
    void shouldReturnMethodNotAllowedForBaseGetRequest() throws Exception {
        mockMvc.perform(get("/api/v1/sleep-logs"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void shouldGetLatestSleepLog() throws Exception {
        SleepLogResponse response = new SleepLogResponse(
            4L,
            "user_123",
            LocalDate.of(2026, 5, 27),
            LocalTime.parse("00:00:00"),
            LocalTime.parse("04:30:00"),
            false,
            Instant.parse("2026-05-30T13:15:00Z")
        );
        when(sleepLogService.getLatestSleepLog("user_123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/sleep-logs/latest").param("userId", "user_123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(4))
            .andExpect(jsonPath("$.wakeUpDate").value("2026-05-27"));
    }

    @Test
    void shouldReturnNotFoundForMissingLatestSleepLog() throws Exception {
        when(sleepLogService.getLatestSleepLog("missing")).thenThrow(new NotFoundException("No sleep logs found"));

        mockMvc.perform(get("/api/v1/sleep-logs/latest").param("userId", "missing"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAnalytics() throws Exception {
        when(sleepLogService.getAnalytics("user_123")).thenReturn(new SleepAnalyticsResponse(
            "user_123",
            4,
            "07:30:00",
            "23:45:00",
            "07:00:00"
        ));

        mockMvc.perform(get("/api/v1/sleep-logs/analytics/averages").param("userId", "user_123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.daysTracked").value(4))
            .andExpect(jsonPath("$.averageSleepDuration").value("07:30:00"));
    }

    @TestConfiguration
    static class MockConfig {

        @Bean
        SleepLogService sleepLogService() {
            return org.mockito.Mockito.mock(SleepLogService.class);
        }
    }
}
