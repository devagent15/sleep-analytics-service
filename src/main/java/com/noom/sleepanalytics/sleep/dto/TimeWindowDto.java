package com.noom.sleepanalytics.sleep.dto;

import java.time.LocalDate;

public record TimeWindowDto(LocalDate startDate, LocalDate endDate) {
}
