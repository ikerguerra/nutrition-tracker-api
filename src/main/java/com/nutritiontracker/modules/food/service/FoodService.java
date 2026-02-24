package com.nutritiontracker.modules.food.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.common.exception.ValidationException;
import com.nutritiontracker.modules.food.dto.FoodRequestDto;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.mapper.FoodMapper;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FoodService {

    private final FoodRepository foodRepository;
    private final com.nutritiontracker.modules.food.repository.ElasticFoodRepository elasticFoodRepository;
    private final FoodMapper foodMapper;

    /**
     * Create a new food
     */
    @Transactional
    @CacheEvict(value = { "frequentFoods", "recentFoods" }, allEntries = true)
    public FoodResponseDto createFood(FoodRequestDto requestDto) {
        log.info("Creating new food: {}", requestDto.getName());

        // Validate barcode uniqueness if provided
        if (requestDto.getBarcode() != null && !requestDto.getBarcode().isBlank()) {
            if (foodRepository.existsByBarcode(requestDto.getBarcode())) {
                throw new ValidationException("Food with barcode '" + requestDto.getBarcode() + "' already exists");
            }
        }

        Food food = foodMapper.toEntity(requestDto);
        Food savedFood = foodRepository.save(food);

        log.info("Food created successfully with id: {}", savedFood.getId());
        return foodMapper.toDto(savedFood);
    }

    /**
     * Get food by ID
     */
    @Cacheable(value = "foodById", key = "#id")
    public FoodResponseDto getFoodById(Long id) {
        log.debug("Fetching food with id: {}", id);

        Food food = foodRepository.findByIdWithNutritionalInfo(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food", id));

        return foodMapper.toDto(food);
    }

    /**
     * Get all foods with pagination
     */
    public Page<FoodResponseDto> getAllFoods(Pageable pageable) {
        log.debug("Fetching all foods with pagination: {}", pageable);

        Page<Food> foods = foodRepository.findAll(pageable);
        return foods.map(foodMapper::toDto);
    }

    /**
     * Search foods by name, brand, and optionally filter by category
     */
    /**
     * Search foods with advanced filters
     */
    public Page<FoodResponseDto> searchFoods(
            String query,
            com.nutritiontracker.modules.food.enums.FoodCategory category,
            java.math.BigDecimal minCalories, java.math.BigDecimal maxCalories,
            java.math.BigDecimal minProtein, java.math.BigDecimal maxProtein,
            java.math.BigDecimal minCarbs, java.math.BigDecimal maxCarbs,
            java.math.BigDecimal minFats, java.math.BigDecimal maxFats,
            Pageable pageable) {

        log.debug("Searching foods with filters: query={}, category={}, minCal={}, maxCal={}",
                query, category, minCalories, maxCalories);

        // If query is present and complex, delegate to Elasticsearch
        if (query != null && !query.trim().isEmpty()) {
            log.debug("Using Elasticsearch for complex text query: {}", query);

            // Remove sort from pageable to let Elasticsearch sort by relevance score
            // Sorting on a text field like 'name' throws fielddata=true exception
            Pageable esPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

            Page<com.nutritiontracker.modules.food.entity.ElasticFoodDocument> elasticPage = elasticFoodRepository
                    .fuzzySearch(query, esPageable);

            if (elasticPage.isEmpty()) {
                return Page.empty(pageable);
            }

            java.util.List<Long> ids = elasticPage.getContent().stream()
                    .map(doc -> Long.valueOf(doc.getId()))
                    .toList();

            // Fetch from MySQL to get full entity info
            java.util.List<Food> foods = foodRepository.findAllById(ids);

            // Re-order based on ElasticSearch relevance score order
            java.util.Map<Long, Food> foodMap = foods.stream()
                    .collect(java.util.stream.Collectors.toMap(Food::getId, f -> f));

            java.util.List<FoodResponseDto> sortedDtos = ids.stream()
                    .map(foodMap::get)
                    .filter(java.util.Objects::nonNull)
                    .map(foodMapper::toDto)
                    .toList();

            return new org.springframework.data.domain.PageImpl<>(sortedDtos, pageable, elasticPage.getTotalElements());
        }

        org.springframework.data.jpa.domain.Specification<Food> spec = com.nutritiontracker.modules.food.repository.FoodSpecifications
                .withFilters(
                        query, category,
                        minCalories, maxCalories,
                        minProtein, maxProtein,
                        minCarbs, maxCarbs,
                        minFats, maxFats);

        Page<Food> foods = foodRepository.findAll(spec, pageable);
        return foods.map(foodMapper::toDto);
    }

    /**
     * Update existing food
     */
    @Transactional
    @CacheEvict(value = { "foodById", "frequentFoods", "recentFoods" }, allEntries = true)
    public FoodResponseDto updateFood(Long id, FoodRequestDto requestDto) {
        log.info("Updating food with id: {}", id);

        Food existingFood = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food", id));

        // Validate barcode uniqueness if changed
        if (requestDto.getBarcode() != null && !requestDto.getBarcode().isBlank()) {
            if (!requestDto.getBarcode().equals(existingFood.getBarcode())) {
                if (foodRepository.existsByBarcode(requestDto.getBarcode())) {
                    throw new ValidationException("Food with barcode '" + requestDto.getBarcode() + "' already exists");
                }
            }
        }

        foodMapper.updateEntityFromDto(requestDto, existingFood);
        Food updatedFood = foodRepository.save(existingFood);

        log.info("Food updated successfully with id: {}", updatedFood.getId());
        return foodMapper.toDto(updatedFood);
    }

    /**
     * Delete food by ID
     */
    @Transactional
    @CacheEvict(value = { "foodById", "frequentFoods", "recentFoods" }, allEntries = true)
    public void deleteFood(Long id) {
        log.info("Deleting food with id: {}", id);

        if (!foodRepository.existsById(id)) {
            throw new ResourceNotFoundException("Food", id);
        }

        foodRepository.deleteById(id);
        log.info("Food deleted successfully with id: {}", id);
    }

    /**
     * Check if food exists by ID
     */
    public boolean existsById(Long id) {
        return foodRepository.existsById(id);
    }

    /**
     * Get total count of foods
     */
    public long getTotalCount() {
        return foodRepository.count();
    }

    /**
     * Calculate nutrition totals based on serving unit and quantity
     */
    public FoodResponseDto.NutritionalInfoDto calculateNutrition(Long foodId, Long servingUnitId,
            java.math.BigDecimal quantity) {
        Food food = foodRepository.findByIdWithNutritionalInfo(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food", foodId));

        java.math.BigDecimal grams;
        if (servingUnitId != null) {
            com.nutritiontracker.modules.food.entity.ServingUnit unit = food.getServingUnits().stream()
                    .filter(u -> u.getId().equals(servingUnitId))
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("Serving unit not found for this food"));
            grams = unit.getWeightGrams().multiply(quantity);
        } else {
            // Default assumes quantity is in grams if no unit specified
            grams = quantity;
        }

        // Base is 100g. Factor = grams / 100
        java.math.BigDecimal factor = grams.divide(java.math.BigDecimal.valueOf(100), 4,
                java.math.RoundingMode.HALF_UP);

        com.nutritiontracker.modules.food.entity.NutritionalInfo base = food.getNutritionalInfo();
        if (base == null) {
            return FoodResponseDto.NutritionalInfoDto.builder()
                    .calories(java.math.BigDecimal.ZERO)
                    .protein(java.math.BigDecimal.ZERO)
                    .carbohydrates(java.math.BigDecimal.ZERO)
                    .fats(java.math.BigDecimal.ZERO)
                    .build();
        }

        return FoodResponseDto.NutritionalInfoDto.builder()
                .calories(safeMultiply(base.getCalories(), factor))
                .protein(safeMultiply(base.getProtein(), factor))
                .carbohydrates(safeMultiply(base.getCarbohydrates(), factor))
                .fats(safeMultiply(base.getFats(), factor))
                .fiber(safeMultiply(base.getFiber(), factor))
                .sugars(safeMultiply(base.getSugars(), factor))
                .saturatedFats(safeMultiply(base.getSaturatedFats(), factor))
                .sodium(safeMultiply(base.getSodium(), factor))
                .calcium(safeMultiply(base.getCalcium(), factor))
                .iron(safeMultiply(base.getIron(), factor))
                .potassium(safeMultiply(base.getPotassium(), factor))
                .vitaminA(safeMultiply(base.getVitaminA(), factor))
                .vitaminC(safeMultiply(base.getVitaminC(), factor))
                .vitaminD(safeMultiply(base.getVitaminD(), factor))
                .vitaminE(safeMultiply(base.getVitaminE(), factor))
                .vitaminB12(safeMultiply(base.getVitaminB12(), factor))
                .build();
    }

    private java.math.BigDecimal safeMultiply(java.math.BigDecimal val, java.math.BigDecimal factor) {
        if (val == null)
            return java.math.BigDecimal.ZERO;
        return val.multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
