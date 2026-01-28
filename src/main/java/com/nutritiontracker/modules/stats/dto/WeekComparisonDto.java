package com.nutritiontracker.modules.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeekComparisonDto {
    private Double caloriesChange; // Percentage change
    private Double proteinChange;
    private Double carbsChange;
    private Double fatsChange;
}
