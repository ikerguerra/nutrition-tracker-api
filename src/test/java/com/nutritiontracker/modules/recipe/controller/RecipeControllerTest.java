package com.nutritiontracker.modules.recipe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.recipe.dto.RecipeRequestDto;
import com.nutritiontracker.modules.recipe.dto.RecipeResponseDto;
import com.nutritiontracker.modules.recipe.entity.Recipe;
import com.nutritiontracker.modules.recipe.service.RecipeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecipeController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecipeControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private RecipeService recipeService;

        @MockBean
        private RecipeMapper recipeMapper;

        // Security beans required by Spring context
        @MockBean
        private com.nutritiontracker.modules.auth.security.JwtTokenProvider jwtTokenProvider;

        @MockBean
        private com.nutritiontracker.modules.auth.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

        /**
         * Inject a mock authenticated user into the Spring SecurityContext so that
         * 
         * @AuthenticationPrincipal User user is populated in controller methods.
         */
        private void setAuthenticatedUser(Long userId) {
                User user = new User();
                user.setId(userId);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                                Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        @DisplayName("GET /api/v1/recipes should return user's recipes")
        void shouldGetUserRecipes() throws Exception {
                setAuthenticatedUser(1L);

                Recipe recipe = new Recipe();
                recipe.setId(1L);
                recipe.setName("Pasta");

                RecipeResponseDto dto = RecipeResponseDto.builder()
                                .id(1L)
                                .name("Pasta")
                                .servings(2)
                                .nutritionPerServing(RecipeResponseDto.NutritionSummaryDto.builder()
                                                .calories(BigDecimal.valueOf(400)).build())
                                .ingredients(List.of())
                                .build();

                when(recipeService.getUserRecipes(1L)).thenReturn(List.of(recipe));
                when(recipeMapper.toDto(recipe)).thenReturn(dto);

                mockMvc.perform(get("/api/v1/recipes"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].id").value(1))
                                .andExpect(jsonPath("$.data[0].name").value("Pasta"));
        }

        @Test
        @DisplayName("GET /api/v1/recipes/{id} should return recipe by ID")
        void shouldGetRecipeById() throws Exception {
                Recipe recipe = new Recipe();
                recipe.setId(5L);
                recipe.setName("Salad");

                RecipeResponseDto dto = RecipeResponseDto.builder()
                                .id(5L)
                                .name("Salad")
                                .servings(1)
                                .ingredients(List.of())
                                .nutritionPerServing(RecipeResponseDto.NutritionSummaryDto.builder().build())
                                .build();

                when(recipeService.getRecipeById(5L)).thenReturn(recipe);
                when(recipeMapper.toDto(recipe)).thenReturn(dto);

                mockMvc.perform(get("/api/v1/recipes/5"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(5))
                                .andExpect(jsonPath("$.data.name").value("Salad"));
        }

        @Test
        @DisplayName("POST /api/v1/recipes should create a new recipe")
        void shouldCreateRecipe() throws Exception {
                setAuthenticatedUser(1L);

                RecipeRequestDto request = RecipeRequestDto.builder()
                                .name("New Recipe")
                                .servings(4)
                                .ingredients(List.of())
                                .build();

                Recipe recipe = new Recipe();
                recipe.setId(10L);
                recipe.setName("New Recipe");

                RecipeResponseDto dto = RecipeResponseDto.builder()
                                .id(10L)
                                .name("New Recipe")
                                .servings(4)
                                .ingredients(List.of())
                                .nutritionPerServing(RecipeResponseDto.NutritionSummaryDto.builder().build())
                                .build();

                when(recipeMapper.toEntity(any(RecipeRequestDto.class), eq(1L))).thenReturn(recipe);
                when(recipeService.createRecipe(any(Recipe.class))).thenReturn(recipe);
                when(recipeService.getRecipeById(10L)).thenReturn(recipe); // Important: re-fetch mock
                when(recipeMapper.toDto(recipe)).thenReturn(dto);

                mockMvc.perform(post("/api/v1/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").value(10))
                                .andExpect(jsonPath("$.message").value("Recipe created successfully"));
        }

        @Test
        @DisplayName("PUT /api/v1/recipes/{id} should update recipe")
        void shouldUpdateRecipe() throws Exception {
                setAuthenticatedUser(1L);

                RecipeRequestDto request = RecipeRequestDto.builder()
                                .name("Updated Recipe")
                                .servings(2)
                                .ingredients(List.of())
                                .build();

                Recipe recipe = new Recipe();
                recipe.setId(1L);
                recipe.setName("Updated Recipe");

                RecipeResponseDto dto = RecipeResponseDto.builder()
                                .id(1L)
                                .name("Updated Recipe")
                                .servings(2)
                                .ingredients(List.of())
                                .nutritionPerServing(RecipeResponseDto.NutritionSummaryDto.builder().build())
                                .build();

                when(recipeMapper.toEntity(any(RecipeRequestDto.class), eq(1L))).thenReturn(recipe);
                when(recipeService.updateRecipe(eq(1L), any(Recipe.class))).thenReturn(recipe);
                when(recipeService.getRecipeById(1L)).thenReturn(recipe); // Important: re-fetch mock
                when(recipeMapper.toDto(recipe)).thenReturn(dto);

                mockMvc.perform(put("/api/v1/recipes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.name").value("Updated Recipe"))
                                .andExpect(jsonPath("$.message").value("Recipe updated successfully"));
        }

        @Test
        @DisplayName("DELETE /api/v1/recipes/{id} should delete recipe")
        void shouldDeleteRecipe() throws Exception {
                doNothing().when(recipeService).deleteRecipe(1L);

                mockMvc.perform(delete("/api/v1/recipes/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Recipe deleted successfully"));
        }
}
