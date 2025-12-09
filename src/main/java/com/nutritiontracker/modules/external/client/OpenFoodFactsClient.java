package com.nutritiontracker.modules.external.client;

import com.nutritiontracker.modules.external.dto.OpenFoodFactsSearchResponse;
import com.nutritiontracker.modules.external.dto.OpenFoodFactsProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class OpenFoodFactsClient {

        private final WebClient webClient;
        private final Duration timeout;

        public OpenFoodFactsClient(
                        WebClient.Builder webClientBuilder,
                        @Value("${external.openfoodfacts.base-url}") String baseUrl,
                        @Value("${external.openfoodfacts.timeout:5000}") long timeoutMillis) {

                this.timeout = Duration.ofMillis(timeoutMillis);
                this.webClient = webClientBuilder
                                .baseUrl(baseUrl)
                                .defaultHeader("User-Agent", "NutritionTracker/1.0")
                                .build();
        }

        public OpenFoodFactsSearchResponse searchProducts(String query, int page, int pageSize) {
                log.debug("Searching OpenFoodFacts for: {}, page: {}, size: {}", query, page, pageSize);

                try {
                        return webClient.get()
                                        .uri(uriBuilder -> uriBuilder
                                                        .path("/cgi/search.pl")
                                                        .queryParam("search_terms", query)
                                                        .queryParam("search_simple", "1")
                                                        .queryParam("action", "process")
                                                        .queryParam("fields",
                                                                        "code,product_name,brands,image_url,nutriments")
                                                        .queryParam("json", "1")
                                                        .queryParam("page", page)
                                                        .queryParam("page_size", pageSize)
                                                        .build())
                                        .retrieve()
                                        .bodyToMono(OpenFoodFactsSearchResponse.class)
                                        .timeout(timeout)
                                        .doOnError(error -> log.warn(
                                                        "Error calling OpenFoodFacts API for query '{}': {}",
                                                        query, error.getMessage()))
                                        .onErrorReturn(new OpenFoodFactsSearchResponse()) // Return empty response on
                                                                                          // error
                                        .block(); // Blocking for now as our service layer is synchronous
                } catch (Exception e) {
                        log.error("Failed to search OpenFoodFacts for query '{}': {}", query, e.getMessage());
                        return new OpenFoodFactsSearchResponse();
                }
        }

        public OpenFoodFactsProduct getProductByBarcode(String barcode) {
                log.debug("Fetching product from OpenFoodFacts by barcode: {}", barcode);

                // The API structure for single product is slightly different, usually
                // /product/{barcode}.json
                // But we can map the response to a wrapper or just the product.
                // Let's assume we get a wrapper and extract the product.

                try {
                        return webClient.get()
                                        .uri(uriBuilder -> uriBuilder
                                                        .path("/api/v0/product/{barcode}.json")
                                                        .queryParam("fields",
                                                                        "code,product_name,brands,image_url,nutriments")
                                                        .build(barcode))
                                        .retrieve()
                                        .bodyToMono(ProductResponseWrapper.class)
                                        .map(ProductResponseWrapper::getProduct)
                                        .timeout(timeout)
                                        .doOnError(error -> log.warn(
                                                        "Error calling OpenFoodFacts API for barcode '{}': {}",
                                                        barcode, error.getMessage()))
                                        .onErrorReturn(null) // Return null on error
                                        .block();
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
