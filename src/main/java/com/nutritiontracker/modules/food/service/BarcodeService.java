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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(readOnly = true)
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
                    .uri("/api/v0/product/{barcode}.json", barcode)
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
        OpenFoodFactsNutriments nutriments = product.getNutriments() != null ? product.getNutriments()
                : new OpenFoodFactsNutriments();

        FoodResponseDto.NutritionalInfoDto nutritionalInfo = FoodResponseDto.NutritionalInfoDto.builder()
                .calories(convertToGrams(nutriments.getEnergyKcal100g()))
                .protein(convertToGrams(nutriments.getProteins100g()))
                .carbohydrates(convertToGrams(nutriments.getCarbohydrates100g()))
                .fats(convertToGrams(nutriments.getFat100g()))
                .fiber(convertToGrams(nutriments.getFiber100g()))
                .sugars(convertToGrams(nutriments.getSugars100g()))
                .saturatedFats(convertToGrams(nutriments.getSaturatedFat100g()))
                .sodium(convertToMg(nutriments.getSodium100g()))
                .calcium(convertToMg(nutriments.getCalcium100g()))
                .iron(convertToMg(nutriments.getIron100g()))
                .potassium(convertToMg(nutriments.getPotassium100g()))
                .vitaminA(convertToMg(nutriments.getVitaminA100g()))
                .vitaminC(convertToMg(nutriments.getVitaminC100g()))
                .vitaminD(convertToMg(nutriments.getVitaminD100g()))
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
        return value != null ? BigDecimal.valueOf(value) : BigDecimal.ZERO;
    }

    private BigDecimal convertToMg(Double value) {
        return value != null ? BigDecimal.valueOf(value * 1000) : BigDecimal.ZERO; // Convert g to mg
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
