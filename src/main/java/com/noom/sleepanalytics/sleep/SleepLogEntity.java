package com.noom.sleepanalytics.sleep;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "sleep_logs",
    uniqueConstraints = @UniqueConstraint(name = "uk_sleep_logs_user_id_wake_up_date", columnNames = {"user_id", "wake_up_date"})
)
public class SleepLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "wake_up_date", nullable = false)
    private LocalDate wakeUpDate;

    @Column(nullable = false)
    private LocalTime bedtime;

    @Column(name = "wake_time", nullable = false)
    private LocalTime wakeTime;

    @Column(name = "is_bedtime_before_midnight", nullable = false)
    private boolean isBedtimeBeforeMidnight;

    @Enumerated(EnumType.STRING)
    @Column(name = "morning_feeling", nullable = false)
    private MorningFeeling morningFeeling;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getWakeUpDate() {
        return wakeUpDate;
    }

    public void setWakeUpDate(LocalDate wakeUpDate) {
        this.wakeUpDate = wakeUpDate;
    }

    public LocalTime getBedtime() {
        return bedtime;
    }

    public void setBedtime(LocalTime bedtime) {
        this.bedtime = bedtime;
    }

    public LocalTime getWakeTime() {
        return wakeTime;
    }

    public void setWakeTime(LocalTime wakeTime) {
        this.wakeTime = wakeTime;
    }

    public boolean isBedtimeBeforeMidnight() {
        return isBedtimeBeforeMidnight;
    }

    public void setBedtimeBeforeMidnight(boolean bedtimeBeforeMidnight) {
        isBedtimeBeforeMidnight = bedtimeBeforeMidnight;
    }

    public MorningFeeling getMorningFeeling() {
        return morningFeeling;
    }

    public void setMorningFeeling(MorningFeeling morningFeeling) {
        this.morningFeeling = morningFeeling;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
