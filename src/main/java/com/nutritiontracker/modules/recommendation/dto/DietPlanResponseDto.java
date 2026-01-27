package com.nutritiontracker.modules.recommendation.dto;

import com.nutritiontracker.modules.recommendation.entity.DietPlan.DietPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietPlanResponseDto {
    private Long id;
    private LocalDate date;
    private Integer version;
    private DietPlanStatus status;
    private List<RecommendedMealDto> meals;
    private NutritionalTotalsDto planTotals;
    private NutritionalTotalsDto dailyGoal;
}
