package com.nutritiontracker.modules.food.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodResponseDto {

    private Long id;
    private String name;
    private String brand;
    private String barcode;
    private BigDecimal servingSize;
    private String servingUnit;
    private com.nutritiontracker.modules.food.enums.FoodCategory category;
    private NutritionalInfoDto nutritionalInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionalInfoDto {
        private Long id;

        // Macronutrients
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbohydrates;
        private BigDecimal fats;
        private BigDecimal fiber;
        private BigDecimal sugars;
        private BigDecimal saturatedFats;

        // Micronutrients
        private BigDecimal sodium;
        private BigDecimal calcium;
        private BigDecimal iron;
        private BigDecimal potassium;
        private BigDecimal vitaminA;
        private BigDecimal vitaminC;
        private BigDecimal vitaminD;
        private BigDecimal vitaminE;
        private BigDecimal vitaminB12;
    }
}
