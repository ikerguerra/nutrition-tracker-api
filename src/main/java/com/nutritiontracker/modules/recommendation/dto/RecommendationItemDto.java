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
    private Long id;
    private Long foodId;
    private String foodName;
    private BigDecimal suggestedQuantity;
    private String unit;
    private String reason;
    private String status;
    private NutritionalTotalsDto nutritionalInfo; // per quantity suggested
}
