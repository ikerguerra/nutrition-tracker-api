package com.nutritiontracker.modules.food.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nutritiontracker.common.exception.ExternalApiException;
import com.nutritiontracker.modules.food.dto.BarcodeSearchResponseDto;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.mapper.FoodMapper;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Barcode Service Unit Tests")
class BarcodeServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private FoodMapper foodMapper;

    private WireMockServer wireMockServer;
    private BarcodeService barcodeService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0); // Dynamic port
        wireMockServer.start();

        // Use a real WebClient builder pointing to our MockWebServer
        WebClient.Builder webClientBuilder = WebClient.builder();
        barcodeService = new BarcodeService(foodRepository, foodMapper, webClientBuilder);

        ReflectionTestUtils.setField(barcodeService, "openFoodFactsBaseUrl", wireMockServer.baseUrl());
        ReflectionTestUtils.setField(barcodeService, "timeout", 5000);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when barcode is null or empty")
    void shouldThrowExceptionWhenBarcodeIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> barcodeService.searchByBarcode(null));
        assertThrows(IllegalArgumentException.class, () -> barcodeService.searchByBarcode("  "));
    }

    @Test
    @DisplayName("Should return local food when found in local database")
    void shouldReturnLocalFood() {
        Food food = new Food();
        food.setBarcode("123");

        FoodResponseDto dto = new FoodResponseDto();
        dto.setBarcode("123");

        when(foodRepository.findByBarcodeWithNutritionalInfo("123")).thenReturn(Optional.of(food));
        when(foodMapper.toDto(food)).thenReturn(dto);

        BarcodeSearchResponseDto result = barcodeService.searchByBarcode("123");

        assertThat(result.isFoundInDatabase()).isTrue();
        assertThat(result.getSource()).isEqualTo("local");
        assertThat(result.getFood()).isNotNull();
        assertThat(result.getFood().getBarcode()).isEqualTo("123");

        verify(foodRepository).findByBarcodeWithNutritionalInfo("123");
    }

    @Test
    @DisplayName("Should return external food when found in Open Food Facts API")
    void shouldReturnExternalFood() {
        when(foodRepository.findByBarcodeWithNutritionalInfo("456")).thenReturn(Optional.empty());

        // Construct mock JSON response
        String jsonResponse = "{\"status\":1,\"product\":{\"product_name\":\"Test Product\",\"brands\":\"BrandX\",\"nutriments\":{\"energy-kcal_100g\":150.0,\"proteins_100g\":5.0}}}";
        
        wireMockServer.stubFor(get(urlEqualTo("/api/v0/product/456.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));

        BarcodeSearchResponseDto result = barcodeService.searchByBarcode("456");

        assertThat(result.isFoundInDatabase()).isFalse();
        assertThat(result.getSource()).isEqualTo("openfoodfacts");
        assertThat(result.getFood()).isNotNull();
        assertThat(result.getFood().getName()).isEqualTo("Test Product");
        assertThat(result.getFood().getBrand()).isEqualTo("BrandX");
        assertThat(result.getFood().getBarcode()).isEqualTo("456");
        assertThat(result.getFood().getNutritionalInfo().getCalories().intValue()).isEqualTo(150);
        assertThat(result.getFood().getNutritionalInfo().getProtein().intValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should return not_found when product is missing everywhere")
    void shouldReturnNotFound() {
        when(foodRepository.findByBarcodeWithNutritionalInfo("789")).thenReturn(Optional.empty());

        // Mock a 'product not found' JSON response from OFF
        String jsonResponse = "{\"status\":0}";
        wireMockServer.stubFor(get(urlEqualTo("/api/v0/product/789.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));

        BarcodeSearchResponseDto result = barcodeService.searchByBarcode("789");

        assertThat(result.isFoundInDatabase()).isFalse();
        assertThat(result.getSource()).isEqualTo("none");
        assertThat(result.getFood()).isNull();
    }

    @Test
    @DisplayName("Should return empty Product when null properties are present in API")
    void shouldReturnProductWithNullProperties() {
        when(foodRepository.findByBarcodeWithNutritionalInfo("000")).thenReturn(Optional.empty());

        // Construct mock JSON response with missing properties
        String jsonResponse = "{\"status\":1,\"product\":{}}";
        
        wireMockServer.stubFor(get(urlEqualTo("/api/v0/product/000.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));

        BarcodeSearchResponseDto result = barcodeService.searchByBarcode("000");

        assertThat(result.isFoundInDatabase()).isFalse();
        assertThat(result.getSource()).isEqualTo("openfoodfacts");
        assertThat(result.getFood()).isNotNull();
        assertThat(result.getFood().getName()).isEqualTo("Unknown Product");
        assertThat(result.getFood().getNutritionalInfo().getCalories().intValue()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle underlying WebClient errors gracefully by returning not found (null mono)")
    void shouldHandleExternalApiException() {
        when(foodRepository.findByBarcodeWithNutritionalInfo("error-code")).thenReturn(Optional.empty());

        wireMockServer.stubFor(get(urlEqualTo("/api/v0/product/error-code.json"))
                .willReturn(aResponse()
                        .withStatus(500)));

        BarcodeSearchResponseDto result = barcodeService.searchByBarcode("error-code");

        // The service catches WebClientResponseException internally via onErrorResume and returns empty mono.
        // This makes externalFood null, resulting in BarcodeSearchResponseDto.notFound
        assertThat(result.isFoundInDatabase()).isFalse();
        assertThat(result.getSource()).isEqualTo("none");
        assertThat(result.getFood()).isNull();
    }

    @Test
    @DisplayName("Should throw ExternalApiException when Open Food Facts throws a hard exception")
    void shouldThrowExternalApiException() {
        when(foodRepository.findByBarcodeWithNutritionalInfo("hard-error")).thenReturn(Optional.empty());

        // Mock WebClient.Builder to throw exception before the inner try-catch
        WebClient.Builder mockBuilder = mock(WebClient.Builder.class);
        when(mockBuilder.baseUrl(anyString())).thenThrow(new RuntimeException("Simulated exception"));
        
        BarcodeService faultyBarcodeService = new BarcodeService(foodRepository, foodMapper, mockBuilder);
        ReflectionTestUtils.setField(faultyBarcodeService, "openFoodFactsBaseUrl", "http://dummy");
        
        assertThrows(ExternalApiException.class, () -> faultyBarcodeService.searchByBarcode("hard-error"));
    }

    @Test
    @DisplayName("Should map correctly when Nutriments object is missing from Open Food Facts API")
    void shouldMapWhenNutrimentsAreNull() {
        when(foodRepository.findByBarcodeWithNutritionalInfo("no-nutriments")).thenReturn(Optional.empty());

        String jsonResponse = "{\"status\":1,\"product\":{\"product_name\":\"Test Product\",\"brands\":\"BrandX\"}}";
        
        wireMockServer.stubFor(get(urlEqualTo("/api/v0/product/no-nutriments.json"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse)));

        BarcodeSearchResponseDto result = barcodeService.searchByBarcode("no-nutriments");

        assertThat(result.isFoundInDatabase()).isFalse();
        assertThat(result.getSource()).isEqualTo("openfoodfacts");
        assertThat(result.getFood().getNutritionalInfo().getCalories().intValue()).isEqualTo(0);
        assertThat(result.getFood().getNutritionalInfo().getCalcium().intValue()).isEqualTo(0);
    }
}
