package com.nutritiontracker.modules.food.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarcodeSearchResponseDto {

    private boolean foundInDatabase;
    private String source; // "local" or "openfoodfacts"
    private FoodResponseDto food;
    private String message;

    public static BarcodeSearchResponseDto notFound(String barcode) {
        return BarcodeSearchResponseDto.builder()
                .foundInDatabase(false)
                .source("none")
                .message("No food found with barcode: " + barcode)
                .build();
    }

    public static BarcodeSearchResponseDto fromLocal(FoodResponseDto food) {
        return BarcodeSearchResponseDto.builder()
                .foundInDatabase(true)
                .source("local")
                .food(food)
                .message("Food found in local database")
                .build();
    }

    public static BarcodeSearchResponseDto fromExternal(FoodResponseDto food) {
        return BarcodeSearchResponseDto.builder()
                .foundInDatabase(false)
                .source("openfoodfacts")
                .food(food)
                .message("Food found in Open Food Facts API")
                .build();
    }
}
