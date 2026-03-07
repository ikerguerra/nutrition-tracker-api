package com.nutritiontracker.modules.recipe.repository;

import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import com.nutritiontracker.modules.recipe.entity.Recipe;
import com.nutritiontracker.modules.recipe.entity.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// Disable Flyway and use Hibernate to create schema — Flyway migrations use
// MySQL syntax
// incompatible with the H2 in-memory database used by @DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Recipe Repository Integration Tests")
class RecipeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private FoodRepository foodRepository;

    private Food persistedFood;

    @BeforeEach
    void setUp() {
        Food food = new Food();
        food.setName("Test Food");
        food.setServingSize(BigDecimal.valueOf(100));
        food.setServingUnit("g");
        persistedFood = entityManager.persistAndFlush(food);
    }

    private Recipe buildRecipeForUser(Long userId, boolean isPublic) {
        Recipe recipe = new Recipe();
        recipe.setUserId(userId);
        recipe.setName("Recipe for user " + userId);
        recipe.setServings(2);
        recipe.setIsPublic(isPublic);
        return recipe;
    }

    private RecipeIngredient buildIngredient(Recipe recipe) {
        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setFood(persistedFood);
        ingredient.setQuantity(BigDecimal.valueOf(100));
        ingredient.setUnit("g");
        ingredient.setRecipe(recipe);
        return ingredient;
    }

    @Test
    @DisplayName("findByUserId should return only recipes belonging to the given user")
    void shouldFindByUserId() {
        Recipe r1 = buildRecipeForUser(1L, false);
        Recipe r2 = buildRecipeForUser(2L, false);
        entityManager.persistAndFlush(r1);
        entityManager.persistAndFlush(r2);
        entityManager.clear();

        List<Recipe> result = recipeRepository.findByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByIdWithIngredients should return recipe with ingredients and food eager-loaded")
    void shouldFindByIdWithIngredients() {
        Recipe recipe = buildRecipeForUser(1L, false);
        RecipeIngredient ingredient = buildIngredient(recipe);
        recipe.addIngredient(ingredient);
        Recipe saved = entityManager.persistAndFlush(recipe);
        entityManager.clear();

        Optional<Recipe> result = recipeRepository.findByIdWithIngredients(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getIngredients()).hasSize(1);
        assertThat(result.get().getIngredients().get(0).getFood().getName()).isEqualTo("Test Food");
    }

    @Test
    @DisplayName("findByIdWithIngredients should return empty when recipe not found")
    void shouldReturnEmptyWhenRecipeNotFound() {
        Optional<Recipe> result = recipeRepository.findByIdWithIngredients(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findPublicOrUserRecipes should return public recipes and user's own recipes")
    void shouldFindPublicOrUserRecipes() {
        Recipe publicRecipe = buildRecipeForUser(99L, true); // other user, public
        Recipe privateRecipe = buildRecipeForUser(99L, false); // other user, private
        Recipe myRecipe = buildRecipeForUser(1L, false); // my recipe, private

        entityManager.persistAndFlush(publicRecipe);
        entityManager.persistAndFlush(privateRecipe);
        entityManager.persistAndFlush(myRecipe);
        entityManager.clear();

        List<Recipe> result = recipeRepository.findPublicOrUserRecipes(1L);

        // Should contain publicRecipe + myRecipe but NOT privateRecipe
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Recipe::getUserId).containsExactlyInAnyOrder(99L, 1L);
    }

    @Test
    @DisplayName("deleteByUserId should remove all recipes for a given user")
    void shouldDeleteByUserId() {
        Recipe r1 = buildRecipeForUser(5L, false);
        Recipe r2 = buildRecipeForUser(5L, true);
        entityManager.persistAndFlush(r1);
        entityManager.persistAndFlush(r2);
        entityManager.clear();

        recipeRepository.deleteByUserId(5L);
        entityManager.flush();

        List<Recipe> remaining = recipeRepository.findByUserId(5L);
        assertThat(remaining).isEmpty();
    }
}
