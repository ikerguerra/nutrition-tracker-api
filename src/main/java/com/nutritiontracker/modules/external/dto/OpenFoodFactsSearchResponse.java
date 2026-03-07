package com.nutritiontracker.modules.external.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenFoodFactsSearchResponse {

    private int count;
    private int page;
    private int page_size;
    private List<OpenFoodFactsProduct> products = new java.util.ArrayList<>();
}
