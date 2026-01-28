package com.nutritiontracker.modules.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalAchievementDto {
    private Integer totalDays;
    private Integer achievedDays;
    private Double achievementRate; // Percentage (0-100)
    private Integer currentStreak;
    private Integer bestStreak;
}
