package com.nutritiontracker.modules.food.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.common.exception.ValidationException;
import com.nutritiontracker.modules.food.dto.FoodRequestDto;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.food.mapper.FoodMapper;
import com.nutritiontracker.modules.food.repository.ElasticFoodRepository;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Food Service Unit Tests")
class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private ElasticFoodRepository elasticFoodRepository;

    @Mock
    private FoodMapper foodMapper;

    @InjectMocks
    private FoodService foodService;

    @Test
    @DisplayName("Should create food successfully when barcode is unique")
    void shouldCreateFoodSuccessfully() {
        // Arrange
        FoodRequestDto request = new FoodRequestDto();
        request.setName("Test Apple");
        request.setBarcode("123456789");

        Food entity = new Food();
        entity.setId(1L);
        entity.setName("Test Apple");
        entity.setBarcode("123456789");

        FoodResponseDto responseDto = new FoodResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Test Apple");

        when(foodRepository.existsByBarcode("123456789")).thenReturn(false);
        when(foodMapper.toEntity(request)).thenReturn(entity);
        when(foodRepository.save(entity)).thenReturn(entity);
        when(foodMapper.toDto(entity)).thenReturn(responseDto);

        // Act
        FoodResponseDto result = foodService.createFood(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Apple");
        verify(foodRepository).save(any(Food.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when trying to create food with existing barcode")
    void shouldThrowExceptionWhenBarcodeExists() {
        // Arrange
        FoodRequestDto request = new FoodRequestDto();
        request.setName("Test Apple");
        request.setBarcode("123456789");

        when(foodRepository.existsByBarcode(anyString())).thenReturn(true);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> foodService.createFood(request));
        assertThat(exception.getMessage()).contains("already exists");
        verify(foodRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return food when valid ID is provided")
    void shouldReturnFoodWhenValidIdProvided() {
        // Arrange
        Food entity = new Food();
        entity.setId(1L);
        entity.setName("Banana");

        FoodResponseDto responseDto = new FoodResponseDto();
        responseDto.setId(1L);
        responseDto.setName("Banana");

        when(foodRepository.findByIdWithNutritionalInfo(1L)).thenReturn(Optional.of(entity));
        when(foodMapper.toDto(entity)).thenReturn(responseDto);

        // Act
        FoodResponseDto result = foodService.getFoodById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Banana");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when non-existent ID is provided")
    void shouldThrowExceptionWhenFoodIdNotFound() {
        // Arrange
        when(foodRepository.findByIdWithNutritionalInfo(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> foodService.getFoodById(999L)
        );
    }

    @Test
    @DisplayName("Should correctly calculate nutrition scaled by grams")
    void shouldCalculateNutritionProperly() {
        // Arrange
        Food food = new Food();
        food.setId(1L);
        NutritionalInfo nutrition = new NutritionalInfo();
        nutrition.setCalories(BigDecimal.valueOf(100)); // 100 kcal per 100g
        nutrition.setProtein(BigDecimal.valueOf(5)); // 5g protein per 100g
        food.setNutritionalInfo(nutrition);

        when(foodRepository.findByIdWithNutritionalInfo(1L)).thenReturn(Optional.of(food));

        // Act: Requesting calculation for 150 grams. So multiplier is 1.5
        FoodResponseDto.NutritionalInfoDto result = foodService.calculateNutrition(1L, null, BigDecimal.valueOf(150));

        // Assert
        // 100 kcal * 1.5 = 150 kcal
        assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(150));
        // 5g * 1.5 = 7.5g
        assertThat(result.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(7.5));
    }

    @Test
    @DisplayName("Should successfully update an existing food")
    void shouldUpdateFoodSuccessfully() {
        Food existingFood = new Food();
        existingFood.setId(1L);
        existingFood.setName("Old Name");

        FoodRequestDto request = new FoodRequestDto();
        request.setName("New Name");

        Food updatedFood = new Food();
        updatedFood.setId(1L);
        updatedFood.setName("New Name");

        FoodResponseDto responseDto = new FoodResponseDto();
        responseDto.setId(1L);
        responseDto.setName("New Name");

        when(foodRepository.findById(1L)).thenReturn(Optional.of(existingFood));
        when(foodRepository.save(any(Food.class))).thenReturn(updatedFood);
        when(foodMapper.toDto(updatedFood)).thenReturn(responseDto);

        FoodResponseDto result = foodService.updateFood(1L, request);

        assertThat(result.getName()).isEqualTo("New Name");
        verify(foodMapper).updateEntityFromDto(request, existingFood);
        verify(foodRepository).save(existingFood);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent food")
    void shouldThrowExceptionWhenUpdatingNonExistentFood() {
        when(foodRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> foodService.updateFood(999L, new FoodRequestDto()));
        verify(foodRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully delete an existing food")
    void shouldDeleteFoodSuccessfully() {
        when(foodRepository.existsById(1L)).thenReturn(true);

        foodService.deleteFood(1L);

        verify(foodRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent food")
    void shouldThrowExceptionWhenDeletingNonExistentFood() {
        when(foodRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> foodService.deleteFood(999L));
        verify(foodRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should return a paginated list of foods")
    void shouldReturnAllFoods() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        java.util.List<Food> foods = java.util.List.of(new Food(), new Food());
        org.springframework.data.domain.Page<Food> page = new org.springframework.data.domain.PageImpl<>(foods,
                pageable, foods.size());

        when(foodRepository.findAll(pageable)).thenReturn(page);
        when(foodMapper.toDto(any(Food.class))).thenReturn(new FoodResponseDto());

        org.springframework.data.domain.Page<FoodResponseDto> result = foodService.getAllFoods(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(foodRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should perform fallback search in MySQL when ElasticSearch has no results")
    void shouldPerformMySQLSearch() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Food> page = new org.springframework.data.domain.PageImpl<>(
                java.util.Collections.emptyList(), pageable, 0);

        when(foodRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(page);

        org.springframework.data.domain.Page<FoodResponseDto> result = foodService.searchFoods(
                null, null, null, null, null, null, null, null, null, null, pageable);

        assertThat(result.isEmpty()).isTrue();
        verify(foodRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should return total count from repository")
    void shouldReturnTotalCount() {
        when(foodRepository.count()).thenReturn(150L);

        long count = foodService.getTotalCount();

        assertThat(count).isEqualTo(150L);
    }

    @Test
    @DisplayName("Should return true when existsById is called and food exists")
    void shouldReturnTrueWhenExistsById() {
        when(foodRepository.existsById(1L)).thenReturn(true);
        boolean exists = foodService.existsById(1L);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should perform search in ElasticSearch when query is provided")
    void shouldPerformElasticSearch() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        com.nutritiontracker.modules.food.entity.ElasticFoodDocument doc = new com.nutritiontracker.modules.food.entity.ElasticFoodDocument();
        doc.setId("1");
        org.springframework.data.domain.Page<com.nutritiontracker.modules.food.entity.ElasticFoodDocument> elasticPage = new org.springframework.data.domain.PageImpl<>(
                java.util.List.of(doc), pageable, 1);

        when(elasticFoodRepository.fuzzySearch(eq("apple"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(elasticPage);

        Food food = new Food();
        food.setId(1L);
        food.setName("Apple");
        when(foodRepository.findAllById(java.util.List.of(1L))).thenReturn(java.util.List.of(food));

        FoodResponseDto dto = new FoodResponseDto();
        dto.setId(1L);
        dto.setName("Apple");
        when(foodMapper.toDto(food)).thenReturn(dto);

        org.springframework.data.domain.Page<FoodResponseDto> result = foodService.searchFoods(
                "apple", null, null, null, null, null, null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(elasticFoodRepository).fuzzySearch(eq("apple"), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when ElasticSearch has no results")
    void shouldReturnEmptyPageWhenElasticSearchEmpty() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<com.nutritiontracker.modules.food.entity.ElasticFoodDocument> elasticPage = org.springframework.data.domain.Page
                .empty(pageable);

        when(elasticFoodRepository.fuzzySearch(eq("unknown"), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(elasticPage);

        org.springframework.data.domain.Page<FoodResponseDto> result = foodService.searchFoods(
                "unknown", null, null, null, null, null, null, null, null, null, pageable);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should throw ValidationException when updating food with existing barcode")
    void shouldThrowExceptionWhenUpdateBarcodeExists() {
        Food existingFood = new Food();
        existingFood.setId(1L);
        existingFood.setBarcode("old-barcode");

        FoodRequestDto request = new FoodRequestDto();
        request.setBarcode("new-barcode");

        when(foodRepository.findById(1L)).thenReturn(Optional.of(existingFood));
        when(foodRepository.existsByBarcode("new-barcode")).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> foodService.updateFood(1L, request));
        assertThat(exception.getMessage()).contains("already exists");
    }

    @Test
    @DisplayName("Should correctly calculate nutrition scaled by serving unit")
    void shouldCalculateNutritionProperlyWithServingUnit() {
        Food food = new Food();
        food.setId(1L);

        NutritionalInfo nutrition = new NutritionalInfo();
        nutrition.setCalories(BigDecimal.valueOf(100)); // 100 kcal per 100g
        food.setNutritionalInfo(nutrition);

        com.nutritiontracker.modules.food.entity.ServingUnit unit = new com.nutritiontracker.modules.food.entity.ServingUnit();
        unit.setId(1L);
        unit.setWeightGrams(BigDecimal.valueOf(50)); // Serving is 50g
        food.setServingUnits(java.util.List.of(unit));

        when(foodRepository.findByIdWithNutritionalInfo(1L)).thenReturn(Optional.of(food));

        // 2 servings * 50g = 100g. Factor = 1.0 (100 kcal)
        FoodResponseDto.NutritionalInfoDto result = foodService.calculateNutrition(1L, 1L, BigDecimal.valueOf(2));

        assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("Should calculate nutrition returning zeros when no nutritional info present")
    void shouldCalculateNutritionZerosWhenNull() {
        Food food = new Food();
        food.setId(1L);
        food.setNutritionalInfo(null);
        when(foodRepository.findByIdWithNutritionalInfo(1L)).thenReturn(Optional.of(food));

        FoodResponseDto.NutritionalInfoDto result = foodService.calculateNutrition(1L, null, BigDecimal.valueOf(100));

        assertThat(result.getCalories()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should successfully update food and bypass barcode uniqueness check if barcode is same")
    void shouldBypassBarcodeCheckIfSame() {
        Food existingFood = new Food();
        existingFood.setId(1L);
        existingFood.setBarcode("same-barcode");

        FoodRequestDto request = new FoodRequestDto();
        request.setBarcode("same-barcode");

        when(foodRepository.findById(1L)).thenReturn(Optional.of(existingFood));
        when(foodRepository.save(any())).thenReturn(existingFood);
        when(foodMapper.toDto(any())).thenReturn(new FoodResponseDto());

        FoodResponseDto result = foodService.updateFood(1L, request);
        assertThat(result).isNotNull();
        verify(foodRepository, never()).existsByBarcode(anyString());
    }

    @Test
    @DisplayName("Should successfully update food when new barcode does not exist")
    void shouldUpdateFoodWhenNewBarcodeDoesNotExist() {
        Food existingFood = new Food();
        existingFood.setId(1L);
        existingFood.setBarcode("old-barcode");

        FoodRequestDto request = new FoodRequestDto();
        request.setBarcode("new-unique-barcode");

        when(foodRepository.findById(1L)).thenReturn(Optional.of(existingFood));
        when(foodRepository.existsByBarcode("new-unique-barcode")).thenReturn(false);
        when(foodRepository.save(any())).thenReturn(existingFood);
        when(foodMapper.toDto(any())).thenReturn(new FoodResponseDto());

        FoodResponseDto result = foodService.updateFood(1L, request);
        assertThat(result).isNotNull();
        verify(foodRepository).existsByBarcode("new-unique-barcode");
    }

    @Test
    @DisplayName("Should throw ValidationException when calculateNutrition has invalid serving unit ID")
    void shouldThrowValidationExceptionWhenServingUnitNotFound() {
        Food food = new Food();
        food.setId(1L);
        food.setServingUnits(java.util.Collections.emptyList());

        when(foodRepository.findByIdWithNutritionalInfo(1L)).thenReturn(Optional.of(food));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> foodService.calculateNutrition(1L, 999L, BigDecimal.valueOf(1)));

        assertThat(exception.getMessage()).contains("Serving unit not found");
    }
}
