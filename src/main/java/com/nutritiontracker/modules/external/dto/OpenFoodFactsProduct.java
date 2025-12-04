package com.nutritiontracker.modules.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenFoodFactsProduct {

    private String code;

    @JsonProperty("product_name")
    private String productName;

    private String brands;

    @JsonProperty("image_url")
    private String imageUrl;

    private Nutriments nutriments;
}
