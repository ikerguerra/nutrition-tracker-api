package com.nutritiontracker.modules.external.service;

import com.nutritiontracker.modules.external.client.OpenFoodFactsClient;
import com.nutritiontracker.modules.external.dto.ExternalFoodDTO;
import com.nutritiontracker.modules.external.dto.OpenFoodFactsProduct;
import com.nutritiontracker.modules.external.dto.OpenFoodFactsSearchResponse;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenFoodFactsService {

    private final OpenFoodFactsClient openFoodFactsClient;
    private final FoodRepository foodRepository;

    public List<ExternalFoodDTO> searchProducts(String query, int page, int size) {
        log.info("Searching external products for: {}", query);
        try {
            OpenFoodFactsSearchResponse response = openFoodFactsClient.searchProducts(query, page, size);

            if (response == null || response.getProducts() == null) {
                return Collections.emptyList();
            }

            // Handle null products list from error response
            if (response.getProducts() == null) {
                return Collections.emptyList();
            }

            return response.getProducts().stream()
                    .filter(this::isValidProduct)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching OpenFoodFacts", e);
            return Collections.emptyList();
        }
    }

    public Optional<ExternalFoodDTO> getProductByBarcode(String barcode) {
        log.info("Fetching external product by barcode: {}", barcode);
        try {
            OpenFoodFactsProduct product = openFoodFactsClient.getProductByBarcode(barcode);
            if (product != null && isValidProduct(product)) {
                return Optional.of(mapToDTO(product));
            }
        } catch (Exception e) {
            log.error("Error fetching product from OpenFoodFacts", e);
        }
        return Optional.empty();
    }

    @Transactional
    public Food importProduct(String barcode) {
        log.info("Importing product with barcode: {}", barcode);

        // Check if already exists
        Optional<Food> existing = foodRepository.findByBarcode(barcode);
        if (existing.isPresent()) {
            log.info("Product already exists locally: {}", barcode);
            return existing.get();
        }

        try {
            OpenFoodFactsProduct externalProduct = openFoodFactsClient.getProductByBarcode(barcode);
            if (externalProduct == null || !isValidProduct(externalProduct)) {
                throw new IllegalArgumentException("Product not found or invalid data in OpenFoodFacts");
            }

            Food food = transformToFood(externalProduct);
            return foodRepository.save(food);
        } catch (Exception e) {
            log.error("Error importing product from OpenFoodFacts: {}", barcode, e);
            throw new IllegalArgumentException("Failed to import product from OpenFoodFacts: " + e.getMessage());
        }
    }

    private boolean isValidProduct(OpenFoodFactsProduct product) {
        return product.getProductName() != null && !product.getProductName().isEmpty() &&
                product.getNutriments() != null &&
                product.getNutriments().getEnergyKcal100g() != null;
    }

    private ExternalFoodDTO mapToDTO(OpenFoodFactsProduct product) {
        return ExternalFoodDTO.builder()
                .barcode(product.getCode())
                .name(product.getProductName())
                .brand(product.getBrands())
                .imageUrl(product.getImageUrl())
                .servingSize(100.0)
                .servingUnit("g")
                .calories(product.getNutriments().getEnergyKcal100g())
                .protein(product.getNutriments().getProteins100g())
                .carbs(product.getNutriments().getCarbohydrates100g())
                .fats(product.getNutriments().getFat100g())
                .fiber(product.getNutriments().getFiber100g())
                .sugar(product.getNutriments().getSugars100g())
                .sodium(product.getNutriments().getSodium100g())
                .source("OPENFOODFACTS")
                .build();
    }

    private Food transformToFood(OpenFoodFactsProduct product) {
        NutritionalInfo nutritionalInfo = NutritionalInfo.builder()
                .calories(BigDecimal.valueOf(product.getNutriments().getEnergyKcal100g()))
                .protein(BigDecimal.valueOf(
                        product.getNutriments().getProteins100g() != null ? product.getNutriments().getProteins100g()
                                : 0))
                .carbohydrates(BigDecimal.valueOf(product.getNutriments().getCarbohydrates100g() != null
                        ? product.getNutriments().getCarbohydrates100g()
                        : 0))
                .fats(BigDecimal.valueOf(
                        product.getNutriments().getFat100g() != null ? product.getNutriments().getFat100g() : 0))
                .fiber(product.getNutriments().getFiber100g() != null
                        ? BigDecimal.valueOf(product.getNutriments().getFiber100g())
                        : BigDecimal.ZERO)
                .sugars(product.getNutriments().getSugars100g() != null
                        ? BigDecimal.valueOf(product.getNutriments().getSugars100g())
                        : BigDecimal.ZERO)
                .sodium(product.getNutriments().getSodium100g() != null
                        ? BigDecimal.valueOf(product.getNutriments().getSodium100g())
                        : BigDecimal.ZERO)
                .build();

        Food food = Food.builder()
                .name(product.getProductName())
                .brand(product.getBrands())
                .barcode(product.getCode())
                .servingSize(BigDecimal.valueOf(100))
                .servingUnit("g")
                .source("OPENFOODFACTS")
                .externalId(product.getCode())
                .lastSyncedAt(LocalDateTime.now())
                .build();

        food.setNutritionalInfo(nutritionalInfo);
        return food;
    }
}
