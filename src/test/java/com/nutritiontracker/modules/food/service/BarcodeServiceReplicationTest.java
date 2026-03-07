package com.nutritiontracker.modules.food.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.nutritiontracker.modules.food.dto.BarcodeSearchResponseDto;
import com.nutritiontracker.modules.food.mapper.FoodMapper;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarcodeServiceReplicationTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private FoodMapper foodMapper;

    private BarcodeService barcodeService;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setUpAll() {
        wireMockServer = new WireMockServer(0); // Random port
        wireMockServer.start();
    }

    @AfterAll
    static void tearDownAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() {
        // Initialize service manually with mocks and a real WebClient builder for
        // WireMock
        barcodeService = new BarcodeService(foodRepository, foodMapper, WebClient.builder());

        // Inject properties that would normally come from application.yml
        ReflectionTestUtils.setField(barcodeService, "openFoodFactsBaseUrl", wireMockServer.baseUrl());
        ReflectionTestUtils.setField(barcodeService, "timeout", 1000); // 1 second timeout
    }

    @Test
    @DisplayName("Should return food when external API responds correctly")
    void testReplicateBarcodeSuccess() {
        // Arrange
        String barcode = "8480000808592";
        String mockResponse = """
                {
                    "status": 1,
                    "product": {
                        "product_name": "Mocked Chocolate",
                        "brands": "MockBrand",
                        "nutriments": {
                            "energy-kcal_100g": 500,
                            "proteins_100g": 5.5,
                            "carbohydrates_100g": 60,
                            "fat_100g": 30
                        }
                    }
                }
                """;

        // Mock DB not found
        when(foodRepository.findByBarcodeWithNutritionalInfo(anyString())).thenReturn(Optional.empty());

        wireMockServer.stubFor(get(urlEqualTo("/api/v0/product/" + barcode + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // Act
        BarcodeSearchResponseDto response = barcodeService.searchByBarcode(barcode);

        // Assert
        assertThat(response.getFood()).isNotNull();
        assertThat(response.getSource()).isEqualTo("openfoodfacts");
        assertThat(response.getFood().getName()).isEqualTo("Mocked Chocolate");
        assertThat(response.getFood().getNutritionalInfo().getCalories().doubleValue()).isEqualTo(500.0);
    }

    @Test
    @DisplayName("Should return not_found when external API responds with 404 or status 0")
    void testReplicateBarcodeNotFound() {
        // Arrange
        String barcode = "1234567890123";
        String mockResponse = """
                {
                    "status": 0,
                    "status_verbose": "product not found"
                }
                """;

        when(foodRepository.findByBarcodeWithNutritionalInfo(anyString())).thenReturn(Optional.empty());

        wireMockServer.stubFor(get(urlEqualTo("/api/v0/product/" + barcode + ".json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mockResponse)));

        // Act
        BarcodeSearchResponseDto response = barcodeService.searchByBarcode(barcode);

        // Assert
        assertThat(response.getFood()).isNull();
        assertThat(response.getSource()).isEqualTo("none");
    }

    @Test
    @DisplayName("Should return not_found when external API times out")
    void testReplicateBarcodeTimeout() {
        // Arrange
        String barcode = "0000000000000";

        when(foodRepository.findByBarcodeWithNutritionalInfo(anyString())).thenReturn(Optional.empty());

        wireMockServer.stubFor(get(urlEqualTo("/api/v0/product/" + barcode + ".json"))
                .willReturn(aResponse()
                        .withFixedDelay(2000) // Delay longer than the 1s timeout
                        .withStatus(200)
                        .withBody("{}")));

        // Act
        BarcodeSearchResponseDto response = barcodeService.searchByBarcode(barcode);

        // Assert
        assertThat(response.getFood()).isNull();
        assertThat(response.getSource()).isEqualTo("none");
    }
}
