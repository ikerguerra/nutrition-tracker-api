package com.nutritiontracker.modules.recipe.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import com.nutritiontracker.modules.recipe.entity.Recipe;
import com.nutritiontracker.modules.recipe.entity.RecipeIngredient;
import com.nutritiontracker.modules.recipe.repository.RecipeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Recipe Service Unit Tests")
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("Should return user recipes successfully")
    void shouldReturnUserRecipes() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setUserId(100L);

        when(recipeRepository.findByUserId(100L)).thenReturn(List.of(recipe));

        List<Recipe> result = recipeService.getUserRecipes(100L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(100L);
        verify(recipeRepository).findByUserId(100L);
    }

    @Test
    @DisplayName("Should return recipe by ID successfully")
    void shouldReturnRecipeById() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);

        when(recipeRepository.findByIdWithIngredients(1L)).thenReturn(Optional.of(recipe));

        Recipe result = recipeService.getRecipeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when recipe ID does not exist")
    void shouldThrowExceptionWhenRecipeNotFound() {
        when(recipeRepository.findByIdWithIngredients(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.getRecipeById(99L));
    }

    @Test
    @DisplayName("Should create recipe and populate food entities")
    void shouldCreateRecipeAndPopulateFoods() {
        Recipe recipe = new Recipe();
        recipe.setName("Pancakes");

        Food mockFood = new Food();
        mockFood.setId(10L);

        RecipeIngredient ingredient = new RecipeIngredient();
        Food lazyFood = new Food();
        lazyFood.setId(10L); // Input food only has ID
        ingredient.setFood(lazyFood);

        recipe.addIngredient(ingredient);

        when(foodRepository.findById(10L)).thenReturn(Optional.of(mockFood));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(recipe);

        Recipe result = recipeService.createRecipe(recipe);

        assertThat(result).isNotNull();
        // Verify that the food was updated to the full mockFood from DB
        assertThat(result.getIngredients().get(0).getFood()).isEqualTo(mockFood);
        verify(foodRepository).findById(10L);
        verify(recipeRepository).save(recipe);
    }

    @Test
    @DisplayName("Should update recipe properties and ingredients")
    void shouldUpdateRecipe() {
        Recipe existingRecipe = new Recipe();
        existingRecipe.setId(1L);
        existingRecipe.setName("Old Name");

        Recipe updatedRecipe = new Recipe();
        updatedRecipe.setName("New Name");
        updatedRecipe.setDescription("New Desc");
        updatedRecipe.setServings(4);
        updatedRecipe.setIsPublic(true);

        RecipeIngredient newIngredient = new RecipeIngredient();
        Food lazyFood = new Food();
        lazyFood.setId(20L);
        newIngredient.setFood(lazyFood);
        updatedRecipe.addIngredient(newIngredient);

        Food mockFood = new Food();
        mockFood.setId(20L);

        when(recipeRepository.findByIdWithIngredients(1L)).thenReturn(Optional.of(existingRecipe));
        when(foodRepository.findById(20L)).thenReturn(Optional.of(mockFood));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(existingRecipe); // Mocking save returns the modified
                                                                                   // existing entity

        Recipe result = recipeService.updateRecipe(1L, updatedRecipe);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo("New Desc");
        assertThat(result.getServings()).isEqualTo(4);
        assertThat(result.getIsPublic()).isTrue();
        assertThat(result.getIngredients()).hasSize(1);

        verify(recipeRepository).findByIdWithIngredients(1L);
        verify(foodRepository).findById(20L);
        verify(recipeRepository).save(existingRecipe);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating recipe with non-existent food")
    void shouldThrowExceptionWhenUpdatingWithInvalidFood() {
        Recipe existingRecipe = new Recipe();
        existingRecipe.setId(1L);
        existingRecipe.setName("Old Name");

        Recipe updatedRecipe = new Recipe();

        RecipeIngredient newIngredient = new RecipeIngredient();
        Food lazyFood = new Food();
        lazyFood.setId(999L); // Invalid food id
        newIngredient.setFood(lazyFood);
        updatedRecipe.addIngredient(newIngredient);

        when(recipeRepository.findByIdWithIngredients(1L)).thenReturn(Optional.of(existingRecipe));
        when(foodRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recipeService.updateRecipe(1L, updatedRecipe));
    }

    @Test
    @DisplayName("Should delete recipe successfully")
    void shouldDeleteRecipe() {
        recipeService.deleteRecipe(1L);
        verify(recipeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should calculate nutrition summary per serving accurately")
    void shouldCalculateNutrition() {
        Recipe recipe = new Recipe();
        recipe.setServings(2); // The recipe makes 2 servings

        // First ingredient: 200g of something (serving size is 100g) -> Ratio = 2.0
        RecipeIngredient ingredient1 = new RecipeIngredient();
        ingredient1.setQuantity(BigDecimal.valueOf(200));

        Food food1 = new Food();
        food1.setServingSize(BigDecimal.valueOf(100));
        NutritionalInfo nutrition1 = new NutritionalInfo();
        nutrition1.setCalories(BigDecimal.valueOf(150)); // Total cals = 300
        nutrition1.setProtein(BigDecimal.valueOf(10)); // Total prot = 20
        nutrition1.setCarbohydrates(BigDecimal.valueOf(20)); // Total carb = 40
        nutrition1.setFats(BigDecimal.valueOf(5)); // Total fat = 10
        food1.setNutritionalInfo(nutrition1);
        ingredient1.setFood(food1);

        // Second ingredient: 50g of something (serving size is 50g) -> Ratio = 1.0
        RecipeIngredient ingredient2 = new RecipeIngredient();
        ingredient2.setQuantity(BigDecimal.valueOf(50));

        Food food2 = new Food();
        food2.setServingSize(BigDecimal.valueOf(50));
        NutritionalInfo nutrition2 = new NutritionalInfo();
        nutrition2.setCalories(BigDecimal.valueOf(100)); // Total cals = 100
        nutrition2.setProtein(BigDecimal.valueOf(5)); // Total prot = 5
        nutrition2.setCarbohydrates(BigDecimal.valueOf(10)); // Total carb = 10
        nutrition2.setFats(BigDecimal.valueOf(2)); // Total fat = 2
        food2.setNutritionalInfo(nutrition2);
        ingredient2.setFood(food2);

        recipe.addIngredient(ingredient1);
        recipe.addIngredient(ingredient2);

        // Calculate
        RecipeService.NutritionSummary summary = recipeService.calculateNutritionPerServing(recipe);

        // Total Recipe: Cals = 400. Prot = 25. Carbs = 50. Fats = 12.
        // Per Serving (2 servings): Cals = 200. Prot = 12.5. Carbs = 25. Fats = 6.
        assertThat(summary.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(summary.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(12.5));
        assertThat(summary.getCarbs()).isEqualByComparingTo(BigDecimal.valueOf(25));
        assertThat(summary.getFats()).isEqualByComparingTo(BigDecimal.valueOf(6));
    }
}
