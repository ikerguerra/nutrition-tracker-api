package com.nutritiontracker.modules.recipe.controller;

import com.nutritiontracker.common.dto.ApiResponse;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.recipe.dto.RecipeRequestDto;
import com.nutritiontracker.modules.recipe.dto.RecipeResponseDto;
import com.nutritiontracker.modules.recipe.entity.Recipe;
// mapper is in same package
import com.nutritiontracker.modules.recipe.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recipe Management", description = "APIs for managing recipes and calculating nutrition")
public class RecipeController {

    private final RecipeService recipeService;
    private final RecipeMapper recipeMapper;

    @GetMapping
    @Operation(summary = "Get user recipes", description = "Retrieves all recipes for the current user")
    public ResponseEntity<ApiResponse<List<RecipeResponseDto>>> getRecipes(
            @AuthenticationPrincipal User user) {

        List<RecipeResponseDto> recipes = recipeService.getUserRecipes(user.getId())
                .stream()
                .map(recipeMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(recipes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recipe by ID", description = "Retrieves a specific recipe with full details")
    public ResponseEntity<ApiResponse<RecipeResponseDto>> getRecipe(
            @PathVariable("id") Long id) {

        Recipe recipe = recipeService.getRecipeById(id);
        return ResponseEntity.ok(ApiResponse.success(recipeMapper.toDto(recipe)));
    }

    @PostMapping
    @Operation(summary = "Create recipe", description = "Creates a new recipe")
    public ResponseEntity<ApiResponse<RecipeResponseDto>> createRecipe(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RecipeRequestDto request) {

        Recipe recipe = recipeMapper.toEntity(request, user.getId());
        Recipe savedRecipe = recipeService.createRecipe(recipe);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recipe created successfully", recipeMapper.toDto(savedRecipe)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recipe", description = "Updates an existing recipe")
    public ResponseEntity<ApiResponse<RecipeResponseDto>> updateRecipe(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long id,
            @Valid @RequestBody RecipeRequestDto request) {

        Recipe recipe = recipeMapper.toEntity(request, user.getId());
        Recipe updatedRecipe = recipeService.updateRecipe(id, recipe);

        return ResponseEntity.ok(ApiResponse.success("Recipe updated successfully", recipeMapper.toDto(updatedRecipe)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recipe", description = "Removes a recipe")
    public ResponseEntity<ApiResponse<Void>> deleteRecipe(
            @PathVariable("id") Long id) {

        recipeService.deleteRecipe(id);
        return ResponseEntity.ok(ApiResponse.success("Recipe deleted successfully", null));
    }
}
