package com.nutritiontracker.modules.external.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OpenFoodFactsSearchResponse {

    private int count;
    private int page;
    private int page_size;
    private List<OpenFoodFactsProduct> products;
}
