package com.nutritiontracker.modules.recipe.repository;

import com.nutritiontracker.modules.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.food f LEFT JOIN FETCH f.nutritionalInfo WHERE r.userId = :userId")
    List<Recipe> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.food f LEFT JOIN FETCH f.nutritionalInfo WHERE r.id = :id")
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);

    @Query("SELECT r FROM Recipe r WHERE r.isPublic = true OR r.userId = :userId")
    List<Recipe> findPublicOrUserRecipes(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
