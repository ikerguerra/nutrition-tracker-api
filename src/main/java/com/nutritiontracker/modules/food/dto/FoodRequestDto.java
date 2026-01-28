package com.nutritiontracker.modules.food.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodRequestDto {

    @NotBlank(message = "Food name is required")
    @Size(max = 255, message = "Food name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Brand name must not exceed 255 characters")
    private String brand;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;

    @DecimalMin(value = "0.0", inclusive = false, message = "Serving size must be greater than 0")
    private BigDecimal servingSize;

    @Size(max = 50, message = "Serving unit must not exceed 50 characters")
    private String servingUnit;

    private com.nutritiontracker.modules.food.enums.FoodCategory category;

    @Valid
    @NotNull(message = "Nutritional information is required")
    private NutritionalInfoDto nutritionalInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionalInfoDto {

        @DecimalMin(value = "0.0", message = "Calories must be non-negative")
        private BigDecimal calories;

        @DecimalMin(value = "0.0", message = "Protein must be non-negative")
        private BigDecimal protein;

        @DecimalMin(value = "0.0", message = "Carbohydrates must be non-negative")
        private BigDecimal carbohydrates;

        @DecimalMin(value = "0.0", message = "Fats must be non-negative")
        private BigDecimal fats;

        @DecimalMin(value = "0.0", message = "Fiber must be non-negative")
        private BigDecimal fiber;

        @DecimalMin(value = "0.0", message = "Sugars must be non-negative")
        private BigDecimal sugars;

        @DecimalMin(value = "0.0", message = "Saturated fats must be non-negative")
        private BigDecimal saturatedFats;

        // Micronutrients
        @DecimalMin(value = "0.0", message = "Sodium must be non-negative")
        private BigDecimal sodium;

        @DecimalMin(value = "0.0", message = "Calcium must be non-negative")
        private BigDecimal calcium;

        @DecimalMin(value = "0.0", message = "Iron must be non-negative")
        private BigDecimal iron;

        @DecimalMin(value = "0.0", message = "Potassium must be non-negative")
        private BigDecimal potassium;

        @DecimalMin(value = "0.0", message = "Vitamin A must be non-negative")
        private BigDecimal vitaminA;

        @DecimalMin(value = "0.0", message = "Vitamin C must be non-negative")
        private BigDecimal vitaminC;

        @DecimalMin(value = "0.0", message = "Vitamin D must be non-negative")
        private BigDecimal vitaminD;

        @DecimalMin(value = "0.0", message = "Vitamin E must be non-negative")
        private BigDecimal vitaminE;

        @DecimalMin(value = "0.0", message = "Vitamin B12 must be non-negative")
        private BigDecimal vitaminB12;
    }
}
