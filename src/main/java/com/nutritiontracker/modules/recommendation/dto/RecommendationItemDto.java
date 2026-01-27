package com.nutritiontracker.modules.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemDto {
    private Long recommendationId;
    private Long foodId;
    private String foodName;
    private BigDecimal quantity;
    private String unit;
    private String reason;
    private NutritionalTotalsDto nutritionPerServing; // per quantity suggested
}
