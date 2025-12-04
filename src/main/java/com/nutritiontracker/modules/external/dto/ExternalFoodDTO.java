package com.nutritiontracker.modules.external.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExternalFoodDTO {

    private String barcode;
    private String name;
    private String brand;
    private String imageUrl;

    // Standardized to 100g
    @Builder.Default
    private Double servingSize = 100.0;

    @Builder.Default
    private String servingUnit = "g";

    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fats;
    private Double fiber;
    private Double sugar;
    private Double sodium;

    @Builder.Default
    private String source = "OPENFOODFACTS";
}
