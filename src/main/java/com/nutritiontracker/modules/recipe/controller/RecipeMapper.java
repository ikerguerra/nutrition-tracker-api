package com.nutritiontracker.modules.recipe.controller;

import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.recipe.dto.RecipeRequestDto;
import com.nutritiontracker.modules.recipe.dto.RecipeResponseDto;
import com.nutritiontracker.modules.recipe.entity.Recipe;
import com.nutritiontracker.modules.recipe.entity.RecipeIngredient;
import com.nutritiontracker.modules.recipe.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecipeMapper {

    private final RecipeService recipeService;

    public Recipe toEntity(RecipeRequestDto dto, Long userId) {
        if (dto == null)
            return null;

        Recipe recipe = Recipe.builder()
                .userId(userId)
                .name(dto.getName())
                .description(dto.getDescription())
                .servings(dto.getServings() != null ? dto.getServings() : 1)
                .prepTime(dto.getPrepTime())
                .cookTime(dto.getCookTime())
                .instructions(dto.getInstructions())
                .imageUrl(dto.getImageUrl())
                .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false)
                .build();

        if (dto.getIngredients() != null) {
            dto.getIngredients().forEach(ingDto -> {
                RecipeIngredient ingredient = RecipeIngredient.builder()
                        .food(Food.builder().id(ingDto.getFoodId()).build())
                        .quantity(ingDto.getQuantity())
                        .unit(ingDto.getUnit())
                        .build();
                recipe.addIngredient(ingredient);
            });
        }

        return recipe;
    }

    public RecipeResponseDto toDto(Recipe entity) {
        if (entity == null)
            return null;

        var nutritionPerServing = recipeService.calculateNutritionPerServing(entity);

        return RecipeResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .servings(entity.getServings())
                .prepTime(entity.getPrepTime())
                .cookTime(entity.getCookTime())
                .instructions(entity.getInstructions())
                .imageUrl(entity.getImageUrl())
                .isPublic(entity.getIsPublic())
                .ingredients(entity.getIngredients().stream()
                        .map(this::toIngredientDto)
                        .collect(Collectors.toList()))
                .nutritionPerServing(RecipeResponseDto.NutritionSummaryDto.builder()
                        .calories(nutritionPerServing.getCalories())
                        .protein(nutritionPerServing.getProtein())
                        .carbs(nutritionPerServing.getCarbs())
                        .fats(nutritionPerServing.getFats())
                        .build())
                .build();
    }

    private RecipeResponseDto.RecipeIngredientResponseDto toIngredientDto(RecipeIngredient ingredient) {
        return RecipeResponseDto.RecipeIngredientResponseDto.builder()
                .id(ingredient.getId())
                .foodId(ingredient.getFood().getId())
                .foodName(ingredient.getFood().getName())
                .quantity(ingredient.getQuantity())
                .unit(ingredient.getUnit())
                .build();
    }
}
