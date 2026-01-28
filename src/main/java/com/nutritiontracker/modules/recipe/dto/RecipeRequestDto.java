package com.nutritiontracker.modules.recipe.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeRequestDto {
    private String name;
    private String description;
    private Integer servings;
    private Integer prepTime;
    private Integer cookTime;
    private String instructions;
    private String imageUrl;
    private Boolean isPublic;
    private List<RecipeIngredientRequestDto> ingredients;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecipeIngredientRequestDto {
        private Long foodId;
        private BigDecimal quantity;
        private String unit;
    }
}
