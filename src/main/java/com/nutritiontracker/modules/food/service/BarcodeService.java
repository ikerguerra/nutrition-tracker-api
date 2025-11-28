package com.nutritiontracker.modules.food.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nutritiontracker.common.exception.ExternalApiException;
import com.nutritiontracker.modules.food.dto.BarcodeSearchResponseDto;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.mapper.FoodMapper;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarcodeService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;
    private final WebClient.Builder webClientBuilder;

    @Value("${external.openfoodfacts.base-url}")
    private String openFoodFactsBaseUrl;

    @Value("${external.openfoodfacts.timeout:5000}")
    private int timeout;

    /**
     * Search for food by barcode
     * First checks local database, then falls back to Open Food Facts API
     */
    public BarcodeSearchResponseDto searchByBarcode(String barcode) {
        log.info("Searching for food with barcode: {}", barcode);

        if (barcode == null || barcode.isBlank()) {
            throw new IllegalArgumentException("Barcode cannot be null or empty");
        }

        // First, check local database
        Optional<Food> localFood = foodRepository.findByBarcodeWithNutritionalInfo(barcode.trim());
        if (localFood.isPresent()) {
            log.info("Food found in local database for barcode: {}", barcode);
            FoodResponseDto foodDto = foodMapper.toDto(localFood.get());
            return BarcodeSearchResponseDto.fromLocal(foodDto);
        }

        // If not found locally, query Open Food Facts API
        log.info("Food not found locally, querying Open Food Facts API for barcode: {}", barcode);
        try {
            FoodResponseDto externalFood = fetchFromOpenFoodFacts(barcode.trim());
            if (externalFood != null) {
                return BarcodeSearchResponseDto.fromExternal(externalFood);
            }
        } catch (Exception e) {
            log.error("Error fetching from Open Food Facts API", e);
            throw new ExternalApiException("Failed to fetch data from Open Food Facts: " + e.getMessage(), e);
        }

        // Not found anywhere
        log.info("Food not found for barcode: {}", barcode);
        return BarcodeSearchResponseDto.notFound(barcode);
    }

    /**
     * Fetch food data from Open Food Facts API
     */
    private FoodResponseDto fetchFromOpenFoodFacts(String barcode) {
        WebClient webClient = webClientBuilder
                .baseUrl(openFoodFactsBaseUrl)
                .build();

        try {
            OpenFoodFactsResponse response = webClient.get()
                    .uri("/product/{barcode}", barcode)
                    .retrieve()
                    .bodyToMono(OpenFoodFactsResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorResume(e -> {
                        log.warn("Error calling Open Food Facts API: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (response != null && response.getStatus() == 1 && response.getProduct() != null) {
                return mapOpenFoodFactsToDto(response.getProduct(), barcode);
            }

            return null;
        } catch (Exception e) {
            log.error("Unexpected error fetching from Open Food Facts", e);
            return null;
        }
    }

    /**
     * Map Open Food Facts product to our DTO
     */
    private FoodResponseDto mapOpenFoodFactsToDto(OpenFoodFactsProduct product, String barcode) {
        FoodResponseDto.NutritionalInfoDto nutritionalInfo = FoodResponseDto.NutritionalInfoDto.builder()
                .calories(convertToGrams(product.getNutriments().getEnergyKcal100g()))
                .protein(convertToGrams(product.getNutriments().getProteins100g()))
                .carbohydrates(convertToGrams(product.getNutriments().getCarbohydrates100g()))
                .fats(convertToGrams(product.getNutriments().getFat100g()))
                .fiber(convertToGrams(product.getNutriments().getFiber100g()))
                .sugars(convertToGrams(product.getNutriments().getSugars100g()))
                .saturatedFats(convertToGrams(product.getNutriments().getSaturatedFat100g()))
                .sodium(convertToMg(product.getNutriments().getSodium100g()))
                .calcium(convertToMg(product.getNutriments().getCalcium100g()))
                .iron(convertToMg(product.getNutriments().getIron100g()))
                .potassium(convertToMg(product.getNutriments().getPotassium100g()))
                .vitaminA(convertToMg(product.getNutriments().getVitaminA100g()))
                .vitaminC(convertToMg(product.getNutriments().getVitaminC100g()))
                .vitaminD(convertToMg(product.getNutriments().getVitaminD100g()))
                .build();

        return FoodResponseDto.builder()
                .name(product.getProductName() != null ? product.getProductName() : "Unknown Product")
                .brand(product.getBrands())
                .barcode(barcode)
                .servingSize(new BigDecimal("100"))
                .servingUnit("g")
                .nutritionalInfo(nutritionalInfo)
                .build();
    }

    private BigDecimal convertToGrams(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private BigDecimal convertToMg(Double value) {
        return value != null ? BigDecimal.valueOf(value * 1000) : null; // Convert g to mg
    }

    // DTOs for Open Food Facts API response
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenFoodFactsResponse {
        private int status;
        private OpenFoodFactsProduct product;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenFoodFactsProduct {
        @JsonProperty("product_name")
        private String productName;
        
        private String brands;
        
        private OpenFoodFactsNutriments nutriments;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class OpenFoodFactsNutriments {
        @JsonProperty("energy-kcal_100g")
        private Double energyKcal100g;
        
        @JsonProperty("proteins_100g")
        private Double proteins100g;
        
        @JsonProperty("carbohydrates_100g")
        private Double carbohydrates100g;
        
        @JsonProperty("fat_100g")
        private Double fat100g;
        
        @JsonProperty("fiber_100g")
        private Double fiber100g;
        
        @JsonProperty("sugars_100g")
        private Double sugars100g;
        
        @JsonProperty("saturated-fat_100g")
        private Double saturatedFat100g;
        
        @JsonProperty("sodium_100g")
        private Double sodium100g;
        
        @JsonProperty("calcium_100g")
        private Double calcium100g;
        
        @JsonProperty("iron_100g")
        private Double iron100g;
        
        @JsonProperty("potassium_100g")
        private Double potassium100g;
        
        @JsonProperty("vitamin-a_100g")
        private Double vitaminA100g;
        
        @JsonProperty("vitamin-c_100g")
        private Double vitaminC100g;
        
        @JsonProperty("vitamin-d_100g")
        private Double vitaminD100g;
    }
}
