package com.nutritiontracker.modules.dailylog.dto;

import com.nutritiontracker.modules.dailylog.enums.MealType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLogResponseDto {

    private Long id;
    private LocalDate date;
    private BigDecimal dailyWeight;
    private DailyTotalsDto totals;
    private DailyGoalsDto goals;
    private Map<MealType, List<MealEntryDto>> meals;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyGoalsDto {
        private BigDecimal calorieGoal;
        private BigDecimal proteinGoal;
        private BigDecimal carbsGoal;
        private BigDecimal fatsGoal;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyTotalsDto {
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fats;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MealEntryDto {
        private Long id;
        private Long foodId;
        private String foodName;
        private String brand;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fats;
    }
}
