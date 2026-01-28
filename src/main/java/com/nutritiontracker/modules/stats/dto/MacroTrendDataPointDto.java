package com.nutritiontracker.modules.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MacroTrendDataPointDto {
    private LocalDate date;
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fats;
    private Integer goalCalories;
    private Double goalProtein;
    private Double goalCarbs;
    private Double goalFats;
    private Double adherencePercentage; // How close to goals (0-100)
}
