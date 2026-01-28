package com.nutritiontracker.modules.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummaryDto {
    private WeekDataDto currentWeek;
    private WeekDataDto previousWeek;
    private WeekComparisonDto changes;
}
