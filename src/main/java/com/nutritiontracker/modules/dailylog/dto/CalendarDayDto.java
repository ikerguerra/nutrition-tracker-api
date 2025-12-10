package com.nutritiontracker.modules.dailylog.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CalendarDayDto {
    private LocalDate date;
    private BigDecimal totalCalories;
    private BigDecimal totalProtein;
    private BigDecimal totalCarbs;
    private BigDecimal totalFats;
    private Boolean isGoalMet;
    private BigDecimal calorieGoal;
}
