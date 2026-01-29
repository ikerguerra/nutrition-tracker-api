package com.nutritiontracker.modules.dailylog.dto;

import com.nutritiontracker.modules.dailylog.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutrientBreakdownDto {
    private MealType mealType;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fats;

    // Percentages relative to the day's total
    private double caloriesPercentage;
    private double proteinPercentage;
    private double carbsPercentage;
    private double fatsPercentage;

    // Micronutrients and sub-macros
    private BigDecimal fiber;
    private BigDecimal sugars;
    private BigDecimal saturatedFats;
    private BigDecimal sodium;
    private BigDecimal calcium;
    private BigDecimal iron;
    private BigDecimal potassium;
    private BigDecimal vitaminA;
    private BigDecimal vitaminC;
    private BigDecimal vitaminD;
    private BigDecimal vitaminE;
    private BigDecimal vitaminB12;
    private BigDecimal magnesium;
    private BigDecimal zinc;
    private BigDecimal vitaminK;
    private BigDecimal vitaminB1;
    private BigDecimal vitaminB2;
    private BigDecimal vitaminB3;
    private BigDecimal vitaminB6;
    private BigDecimal vitaminB9;
}
