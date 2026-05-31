package com.noom.sleepanalytics.sleep;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SleepLogRepository extends JpaRepository<SleepLogEntity, Long> {

    Optional<SleepLogEntity> findFirstByUserIdOrderByWakeUpDateDescUpdatedAtDescIdDesc(String userId);

    Optional<SleepLogEntity> findByUserIdAndWakeUpDate(String userId, LocalDate wakeUpDate);

    List<SleepLogEntity> findAllByUserIdAndWakeUpDateBetweenOrderByWakeUpDateAsc(
        String userId,
        LocalDate startDate,
        LocalDate endDate
    );
}
