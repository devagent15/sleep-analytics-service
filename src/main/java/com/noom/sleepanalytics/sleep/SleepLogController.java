package com.noom.sleepanalytics.sleep;

import com.noom.sleepanalytics.sleep.dto.CreateSleepLogRequest;
import com.noom.sleepanalytics.sleep.dto.SleepAnalyticsResponse;
import com.noom.sleepanalytics.sleep.dto.SleepLogResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sleep-logs")
public class SleepLogController {

    private static final Logger log = LoggerFactory.getLogger(SleepLogController.class);

    private final SleepLogService sleepLogService;

    public SleepLogController(SleepLogService sleepLogService) {
        this.sleepLogService = sleepLogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SleepLogResponse createSleepLog(@Valid @RequestBody CreateSleepLogRequest request) {
        log.info(
            "Received create sleep log request for userId={} wakeUpDate={}",
            request.userId(),
            request.wakeUpDate()
        );
        return sleepLogService.createSleepLog(request);
    }

    @GetMapping("/latest")
    public SleepLogResponse getLatestSleepLog(@RequestParam String userId) {
        log.info("Received latest sleep log request for userId={}", userId);
        return sleepLogService.getLatestSleepLog(userId);
    }

    @GetMapping("/analytics/averages")
    public SleepAnalyticsResponse getAnalytics(@RequestParam String userId) {
        log.info("Received sleep analytics request for userId={}", userId);
        return sleepLogService.getAnalytics(userId);
    }
}
