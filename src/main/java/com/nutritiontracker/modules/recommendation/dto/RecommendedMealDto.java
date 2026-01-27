package com.nutritiontracker.modules.recommendation.dto;

import com.nutritiontracker.modules.dailylog.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedMealDto {
    private MealType mealType;
    private List<RecommendationItemDto> items;
    private NutritionalTotalsDto totals;
}
