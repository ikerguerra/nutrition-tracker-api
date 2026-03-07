package com.nutritiontracker.modules.food.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutritiontracker.modules.food.dto.BarcodeSearchResponseDto;
import com.nutritiontracker.modules.food.dto.FoodRequestDto;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.service.BarcodeService;
import com.nutritiontracker.modules.food.service.FoodService;
import com.nutritiontracker.modules.food.service.FoodStatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FoodController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller testing
class FoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FoodService foodService;

    @MockBean
    private FoodStatsService foodStatsService;

    @MockBean
    private BarcodeService barcodeService;

    // Security beans that Spring Boot tries to instantiate
    @MockBean
    private com.nutritiontracker.modules.auth.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.nutritiontracker.modules.auth.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    @Test
    @DisplayName("GET /api/v1/foods/{id} should return food")
    void shouldReturnFoodById() throws Exception {
        FoodResponseDto responseDto = new FoodResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Test Apple");

        when(foodService.getFoodById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/foods/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Test Apple"));
    }

    @Test
    @DisplayName("POST /api/v1/foods should create food")
    void shouldCreateFood() throws Exception {

        FoodRequestDto.NutritionalInfoDto infoDto = FoodRequestDto.NutritionalInfoDto.builder()
                .calories(BigDecimal.valueOf(100))
                .protein(BigDecimal.valueOf(10))
                .carbohydrates(BigDecimal.valueOf(10))
                .fats(BigDecimal.valueOf(10))
                .build();

        FoodRequestDto requestDto = new FoodRequestDto();
        requestDto.setName("New Food");
        requestDto.setBrand("BrandX");
        requestDto.setNutritionalInfo(infoDto);

        FoodResponseDto responseDto = new FoodResponseDto();
        responseDto.setId(2L);
        responseDto.setName("New Food");

        when(foodService.createFood(any(FoodRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/foods")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.message").value("Food created successfully"));
    }

    @Test
    @DisplayName("GET /api/v1/foods/barcode/{barcode} should return barcode search result")
    void shouldSearchByBarcode() throws Exception {
        BarcodeSearchResponseDto responseDto = BarcodeSearchResponseDto.builder()
                .foundInDatabase(true)
                .source("local")
                .message("Found in local")
                .build();

        when(barcodeService.searchByBarcode("12345")).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/foods/barcode/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.foundInDatabase").value(true))
                .andExpect(jsonPath("$.data.source").value("local"));
    }

    @Test
    @DisplayName("GET /api/v1/foods/{id}/calculate should return nutritional calculation")
    void shouldCalculateNutrition() throws Exception {
        FoodResponseDto.NutritionalInfoDto infoDto = FoodResponseDto.NutritionalInfoDto.builder()
                .calories(BigDecimal.valueOf(250))
                .protein(BigDecimal.valueOf(10))
                .build();

        when(foodService.calculateNutrition(eq(1L), eq(null), any(BigDecimal.class))).thenReturn(infoDto);

        mockMvc.perform(get("/api/v1/foods/1/calculate")
                .param("quantity", "150.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.calories").value(250))
                .andExpect(jsonPath("$.data.protein").value(10));
    }
}
