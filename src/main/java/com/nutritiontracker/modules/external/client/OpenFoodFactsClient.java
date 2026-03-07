package com.nutritiontracker.modules.external.client;

import com.nutritiontracker.modules.external.dto.OpenFoodFactsSearchResponse;
import com.nutritiontracker.modules.external.dto.OpenFoodFactsProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Component
@Slf4j
public class OpenFoodFactsClient {

        private final RestTemplate restTemplate;
        private final String baseUrl;

        public OpenFoodFactsClient(
                        RestTemplateBuilder restTemplateBuilder,
                        @Value("${external.openfoodfacts.base-url}") String baseUrl,
                        @Value("${external.openfoodfacts.timeout:5000}") long timeoutMillis) {

                this.baseUrl = baseUrl;
                this.restTemplate = restTemplateBuilder
                                .setConnectTimeout(Duration.ofMillis(timeoutMillis))
                                .setReadTimeout(Duration.ofMillis(timeoutMillis))
                                .defaultHeader("User-Agent", "NutritionTrackerApp/1.0 (ikerguerra@hotmail.es)")
                                .defaultHeader("Accept", "application/json")
                                .build();
        }

        public OpenFoodFactsSearchResponse searchProducts(String query, int page, int pageSize) {
                log.debug("Searching OpenFoodFacts for: {}, page: {}, size: {}", query, page, pageSize);

                try {
                        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                                        .path("/cgi/search.pl")
                                        .queryParam("search_terms", query)
                                        .queryParam("search_simple", "1")
                                        .queryParam("action", "process")
                                        .queryParam("fields", "code,product_name,brands,image_url,nutriments")
                                        .queryParam("json", "1")
                                        .queryParam("page", page)
                                        .queryParam("page_size", pageSize)
                                        .toUriString();

                        OpenFoodFactsSearchResponse response = restTemplate.getForObject(url,
                                        OpenFoodFactsSearchResponse.class);
                        return response != null ? response : new OpenFoodFactsSearchResponse();
                } catch (Exception e) {
                        log.error("Failed to search OpenFoodFacts for query '{}': {}", query, e.getMessage());
                        return new OpenFoodFactsSearchResponse();
                }
        }

        public OpenFoodFactsProduct getProductByBarcode(String barcode) {
                log.debug("Fetching product from OpenFoodFacts by barcode: {}", barcode);

                try {
                        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                                        .path("/api/v0/product/{barcode}.json")
                                        .queryParam("fields", "code,product_name,brands,image_url,nutriments")
                                        .buildAndExpand(barcode)
                                        .toUriString();

                        ProductResponseWrapper wrapper = restTemplate.getForObject(url, ProductResponseWrapper.class);
                        return wrapper != null ? wrapper.getProduct() : null;
                } catch (Exception e) {
                        log.error("Failed to fetch product from OpenFoodFacts for barcode '{}': {}", barcode,
                                        e.getMessage());
                        return null;
                }
        }

        // Helper class to unwrap single product response
        @lombok.Data
        @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
        private static class ProductResponseWrapper {
                private String code;
                private OpenFoodFactsProduct product;
                private int status;
                private String status_verbose;
        }
}
