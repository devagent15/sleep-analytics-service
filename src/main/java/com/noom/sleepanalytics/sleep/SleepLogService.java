package com.noom.sleepanalytics.sleep;

import com.noom.sleepanalytics.common.exception.NotFoundException;
import com.noom.sleepanalytics.sleep.analytics.SleepAnalyticsCalculator;
import com.noom.sleepanalytics.sleep.dto.CreateSleepLogRequest;
import com.noom.sleepanalytics.sleep.dto.SleepAnalyticsResponse;
import com.noom.sleepanalytics.sleep.dto.SleepLogResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SleepLogService {

    private static final Logger log = LoggerFactory.getLogger(SleepLogService.class);

    private final SleepLogRepository sleepLogRepository;
    private final SleepAnalyticsCalculator sleepAnalyticsCalculator;
    private final Clock clock;

    public SleepLogService(
        SleepLogRepository sleepLogRepository,
        SleepAnalyticsCalculator sleepAnalyticsCalculator,
        Clock clock
    ) {
        this.sleepLogRepository = sleepLogRepository;
        this.sleepAnalyticsCalculator = sleepAnalyticsCalculator;
        this.clock = clock;
    }

    public SleepLogResponse createSleepLog(CreateSleepLogRequest request) {
        SleepLogEntity sleepLogEntity = new SleepLogEntity();
        sleepLogEntity.setUserId(request.userId());
        sleepLogEntity.setWakeUpDate(request.wakeUpDate());
        sleepLogEntity.setBedtime(request.bedtime());
        sleepLogEntity.setWakeTime(request.wakeTime());
        sleepLogEntity.setBedtimeBeforeMidnight(request.isBedtimeBeforeMidnight());

        SleepLogResponse response = toResponse(sleepLogRepository.save(sleepLogEntity));
        log.info(
            "Created sleep log id={} for userId={} wakeUpDate={}",
            response.id(),
            response.userId(),
            response.wakeUpDate()
        );
        return response;
    }

    public SleepLogResponse getLatestSleepLog(String userId) {
        SleepLogResponse response = sleepLogRepository.findFirstByUserIdOrderByWakeUpDateDesc(userId)
            .map(this::toResponse)
            .orElseThrow(() -> new NotFoundException("No sleep logs found for userId=" + userId));
        log.info(
            "Fetched latest sleep log id={} for userId={} wakeUpDate={}",
            response.id(),
            response.userId(),
            response.wakeUpDate()
        );
        return response;
    }

    public SleepAnalyticsResponse getAnalytics(String userId) {
        LocalDate endDate = LocalDate.now(clock);
        LocalDate startDate = endDate.minusDays(29);
        List<SleepLogEntity> sleepLogs = sleepLogRepository.findAllByUserIdAndWakeUpDateBetweenOrderByWakeUpDateAsc(
            userId,
            startDate,
            endDate
        );
        SleepAnalyticsResponse response = sleepAnalyticsCalculator.calculate(userId, sleepLogs);
        log.info(
            "Calculated analytics for userId={} daysTracked={} window={}..{}",
            userId,
            response.daysTracked(),
            startDate,
            endDate
        );
        return response;
    }

    private SleepLogResponse toResponse(SleepLogEntity sleepLogEntity) {
        return new SleepLogResponse(
            sleepLogEntity.getId(),
            sleepLogEntity.getUserId(),
            sleepLogEntity.getWakeUpDate(),
            sleepLogEntity.getBedtime(),
            sleepLogEntity.getWakeTime(),
            sleepLogEntity.isBedtimeBeforeMidnight(),
            sleepLogEntity.getCreatedAt()
        );
    }
}
