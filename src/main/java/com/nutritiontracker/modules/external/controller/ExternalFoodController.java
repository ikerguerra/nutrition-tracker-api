package com.nutritiontracker.modules.external.controller;

import com.nutritiontracker.common.dto.ApiResponse;
import com.nutritiontracker.modules.external.dto.ExternalFoodDTO;
import com.nutritiontracker.modules.external.service.OpenFoodFactsService;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.mapper.FoodMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/external/foods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "External Food Integration", description = "APIs for searching and importing foods from OpenFoodFacts")
public class ExternalFoodController {

    private final OpenFoodFactsService openFoodFactsService;
    private final FoodMapper foodMapper;

    @GetMapping("/search")
    @Operation(summary = "Search in OpenFoodFacts", description = "Search for products in the OpenFoodFacts database")
    public ResponseEntity<ApiResponse<List<ExternalFoodDTO>>> search(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        log.info("REST request to search external foods: {}", query);
        List<ExternalFoodDTO> results = openFoodFactsService.searchProducts(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get by barcode from OpenFoodFacts", description = "Fetch a specific product details from OpenFoodFacts")
    public ResponseEntity<ApiResponse<ExternalFoodDTO>> getByBarcode(
            @Parameter(description = "Product barcode") @PathVariable("barcode") String barcode) {

        log.info("REST request to get external food by barcode: {}", barcode);
        return openFoodFactsService.getProductByBarcode(barcode)
                .map(food -> ResponseEntity.ok(ApiResponse.success(food)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{barcode}/import")
    @Operation(summary = "Import from OpenFoodFacts", description = "Import a product from OpenFoodFacts to local database")
    public ResponseEntity<ApiResponse<FoodResponseDto>> importProduct(
            @Parameter(description = "Product barcode") @PathVariable("barcode") String barcode) {

        log.info("REST request to import external food: {}", barcode);
        try {
            Food importedFood = openFoodFactsService.importProduct(barcode);
            return ResponseEntity
                    .ok(ApiResponse.success("Product imported successfully", foodMapper.toDto(importedFood)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/import-batch")
    @Operation(summary = "Import in batch from OpenFoodFacts", description = "Import products from OpenFoodFacts in batch based on a search query")
    public ResponseEntity<ApiResponse<String>> importBatch(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Max pages to process") @RequestParam(defaultValue = "1") int maxPages) {

        log.info("REST request to import batch for query: {} (maxPages: {})", query, maxPages);
        openFoodFactsService.importBatch(query, maxPages);
        return ResponseEntity.ok(ApiResponse.success("Batch import started in background"));
    }

    @PostMapping("/import-featured")
    @Operation(summary = "Import featured categories", description = "Import products for a predefined set of featured categories")
    public ResponseEntity<ApiResponse<String>> importFeatured() {

        log.info("REST request to import featured categories");
        openFoodFactsService.importFeaturedCategories();
        return ResponseEntity.ok(ApiResponse.success("Featured categories import started in background"));
    }
}
