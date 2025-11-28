package com.nutritiontracker.modules.food.controller;

import com.nutritiontracker.common.dto.ApiResponse;
import com.nutritiontracker.modules.food.dto.BarcodeSearchResponseDto;
import com.nutritiontracker.modules.food.dto.FoodRequestDto;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.service.BarcodeService;
import com.nutritiontracker.modules.food.service.FoodService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/foods")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Food Management", description = "APIs for managing food items and nutritional information")
public class FoodController {

    private final FoodService foodService;
    private final BarcodeService barcodeService;

    @PostMapping
    @Operation(summary = "Create a new food", description = "Creates a new food item with nutritional information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Food created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ApiResponse<FoodResponseDto>> createFood(
            @Valid @RequestBody FoodRequestDto requestDto) {
        
        log.info("REST request to create food: {}", requestDto.getName());
        FoodResponseDto createdFood = foodService.createFood(requestDto);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Food created successfully", createdFood));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get food by ID", description = "Retrieves a food item by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Food found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Food not found")
    })
    public ResponseEntity<ApiResponse<FoodResponseDto>> getFoodById(
            @Parameter(description = "ID of the food to retrieve") @PathVariable Long id) {
        
        log.info("REST request to get food by id: {}", id);
        FoodResponseDto food = foodService.getFoodById(id);
        
        return ResponseEntity.ok(ApiResponse.success(food));
    }

    @GetMapping
    @Operation(summary = "Get all foods", description = "Retrieves all food items with pagination and sorting")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Foods retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<FoodResponseDto>>> getAllFoods(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "asc") String direction) {
        
        log.info("REST request to get all foods - page: {}, size: {}, sortBy: {}, direction: {}", 
                page, size, sortBy, direction);
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<FoodResponseDto> foods = foodService.getAllFoods(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(foods));
    }

    @GetMapping("/search")
    @Operation(summary = "Search foods", description = "Search foods by name or brand")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<ApiResponse<Page<FoodResponseDto>>> searchFoods(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("REST request to search foods with query: {}", query);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<FoodResponseDto> foods = foodService.searchFoods(query, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(foods));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Search food by barcode", 
               description = "Search for a food item by barcode. Checks local database first, then Open Food Facts API")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Food not found")
    })
    public ResponseEntity<ApiResponse<BarcodeSearchResponseDto>> searchByBarcode(
            @Parameter(description = "Barcode to search for") @PathVariable String barcode) {
        
        log.info("REST request to search food by barcode: {}", barcode);
        BarcodeSearchResponseDto result = barcodeService.searchByBarcode(barcode);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update food", description = "Updates an existing food item")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Food updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Food not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<ApiResponse<FoodResponseDto>> updateFood(
            @Parameter(description = "ID of the food to update") @PathVariable Long id,
            @Valid @RequestBody FoodRequestDto requestDto) {
        
        log.info("REST request to update food with id: {}", id);
        FoodResponseDto updatedFood = foodService.updateFood(id, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success("Food updated successfully", updatedFood));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete food", description = "Deletes a food item by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Food deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Food not found")
    })
    public ResponseEntity<Void> deleteFood(
            @Parameter(description = "ID of the food to delete") @PathVariable Long id) {
        
        log.info("REST request to delete food with id: {}", id);
        foodService.deleteFood(id);
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    @Operation(summary = "Get total food count", description = "Returns the total number of foods in the database")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Long>> getTotalCount() {
        log.info("REST request to get total food count");
        long count = foodService.getTotalCount();
        
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
