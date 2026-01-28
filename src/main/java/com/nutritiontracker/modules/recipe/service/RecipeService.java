package com.nutritiontracker.modules.recipe.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.recipe.entity.Recipe;
import com.nutritiontracker.modules.recipe.entity.RecipeIngredient;
import com.nutritiontracker.modules.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public List<Recipe> getUserRecipes(Long userId) {
        return recipeRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Recipe getRecipeById(Long id) {
        return recipeRepository.findByIdWithIngredients(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe", id));
    }

    @Transactional
    public Recipe createRecipe(Recipe recipe) {
        log.info("Creating new recipe: {}", recipe.getName());
        return recipeRepository.save(recipe);
    }

    @Transactional
    public Recipe updateRecipe(Long id, Recipe updatedRecipe) {
        Recipe existing = getRecipeById(id);
        existing.setName(updatedRecipe.getName());
        existing.setDescription(updatedRecipe.getDescription());
        existing.setServings(updatedRecipe.getServings());
        existing.setPrepTime(updatedRecipe.getPrepTime());
        existing.setCookTime(updatedRecipe.getCookTime());
        existing.setInstructions(updatedRecipe.getInstructions());
        existing.setImageUrl(updatedRecipe.getImageUrl());
        existing.setIsPublic(updatedRecipe.getIsPublic());

        existing.getIngredients().clear();
        for (RecipeIngredient ingredient : updatedRecipe.getIngredients()) {
            existing.addIngredient(ingredient);
        }

        return recipeRepository.save(existing);
    }

    @Transactional
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }

    public NutritionSummary calculateNutritionPerServing(Recipe recipe) {
        BigDecimal totalCals = BigDecimal.ZERO;
        BigDecimal totalProt = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFats = BigDecimal.ZERO;

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            BigDecimal servingSize = ingredient.getFood().getServingSize();
            BigDecimal ratio = ingredient.getQuantity().divide(servingSize, 4, RoundingMode.HALF_UP);

            var info = ingredient.getFood().getNutritionalInfo();
            totalCals = totalCals.add(info.getCalories().multiply(ratio));
            totalProt = totalProt.add(info.getProtein().multiply(ratio));
            totalCarbs = totalCarbs.add(info.getCarbohydrates().multiply(ratio));
            totalFats = totalFats.add(info.getFats().multiply(ratio));
        }

        BigDecimal servings = BigDecimal.valueOf(recipe.getServings());
        return NutritionSummary.builder()
                .calories(totalCals.divide(servings, 2, RoundingMode.HALF_UP))
                .protein(totalProt.divide(servings, 2, RoundingMode.HALF_UP))
                .carbs(totalCarbs.divide(servings, 2, RoundingMode.HALF_UP))
                .fats(totalFats.divide(servings, 2, RoundingMode.HALF_UP))
                .build();
    }

    @lombok.Builder
    @lombok.Getter
    public static class NutritionSummary {
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fats;
    }
}
