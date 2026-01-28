package com.nutritiontracker.modules.recipe.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer servings;
    private Integer prepTime;
    private Integer cookTime;
    private String instructions;
    private String imageUrl;
    private Boolean isPublic;
    private List<RecipeIngredientResponseDto> ingredients;
    private NutritionSummaryDto nutritionPerServing;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeIngredientResponseDto {
        private Long id;
        private Long foodId;
        private String foodName;
        private BigDecimal quantity;
        private String unit;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NutritionSummaryDto {
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fats;
    }
}
