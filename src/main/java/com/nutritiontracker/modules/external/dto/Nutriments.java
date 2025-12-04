package com.nutritiontracker.modules.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Nutriments {

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

    @JsonProperty("sodium_100g")
    private Double sodium100g;
}
