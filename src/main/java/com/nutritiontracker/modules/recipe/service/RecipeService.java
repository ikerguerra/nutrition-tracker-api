package com.nutritiontracker.modules.recipe.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.recipe.entity.Recipe;
import com.nutritiontracker.modules.recipe.entity.RecipeIngredient;
import com.nutritiontracker.modules.recipe.repository.RecipeRepository;
import com.nutritiontracker.modules.food.repository.FoodRepository;
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
    private final FoodRepository foodRepository;

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
        populateFoods(recipe);
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

        populateFoods(existing);

        return recipeRepository.save(existing);
    }

    private void populateFoods(Recipe recipe) {
        if (recipe.getIngredients() != null) {
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                if (ingredient.getFood() != null && ingredient.getFood().getId() != null) {
                    ingredient.setFood(foodRepository.findById(ingredient.getFood().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Food", ingredient.getFood().getId())));
                }
            }
        }
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

        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return NutritionSummary.builder().calories(BigDecimal.ZERO).protein(BigDecimal.ZERO).carbs(BigDecimal.ZERO)
                    .fats(BigDecimal.ZERO).build();
        }

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (ingredient.getFood() == null || ingredient.getFood().getServingSize() == null ||
                    ingredient.getFood().getServingSize().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("Food id {} has invalid serving size: {}",
                        ingredient.getFood() != null ? ingredient.getFood().getId() : "null",
                        ingredient.getFood() != null ? ingredient.getFood().getServingSize() : "null");
                continue;
            }

            BigDecimal servingSize = ingredient.getFood().getServingSize();
            BigDecimal ratio = ingredient.getQuantity().divide(servingSize, 4, RoundingMode.HALF_UP);

            var info = ingredient.getFood().getNutritionalInfo();
            if (info != null) {
                if (info.getCalories() != null)
                    totalCals = totalCals.add(info.getCalories().multiply(ratio));
                if (info.getProtein() != null)
                    totalProt = totalProt.add(info.getProtein().multiply(ratio));
                if (info.getCarbohydrates() != null)
                    totalCarbs = totalCarbs.add(info.getCarbohydrates().multiply(ratio));
                if (info.getFats() != null)
                    totalFats = totalFats.add(info.getFats().multiply(ratio));
            }
        }

        BigDecimal servings = BigDecimal.valueOf(recipe.getServings() != null ? recipe.getServings() : 1);
        if (servings.compareTo(BigDecimal.ZERO) <= 0)
            servings = BigDecimal.ONE;

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
