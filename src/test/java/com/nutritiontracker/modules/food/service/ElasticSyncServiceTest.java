package com.nutritiontracker.modules.food.service;

import com.nutritiontracker.modules.food.entity.ElasticFoodDocument;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.food.enums.FoodCategory;
import com.nutritiontracker.modules.food.repository.ElasticFoodRepository;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Elastic Sync Service Unit Tests")
class ElasticSyncServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private ElasticFoodRepository elasticFoodRepository;

    @InjectMocks
    private ElasticSyncService elasticSyncService;

    @Test
    @DisplayName("Should skip sync if elastic search already contains foods")
    void shouldSkipSyncIfNotEmpty() {
        when(elasticFoodRepository.count()).thenReturn(10L);

        elasticSyncService.syncAllFoodsOnStartup();

        verify(foodRepository, never()).findAll();
        verify(elasticFoodRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should skip sync if MySQL repository is empty")
    void shouldSkipSyncIfMySqlIsEmpty() {
        when(elasticFoodRepository.count()).thenReturn(0L);
        when(foodRepository.findAll()).thenReturn(Collections.emptyList());

        elasticSyncService.syncAllFoodsOnStartup();

        verify(elasticFoodRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should sync all foods to Elastic Search properly")
    void shouldSyncFoodsToElasticSearch() {
        when(elasticFoodRepository.count()).thenReturn(0L);

        Food food = new Food();
        food.setId(1L);
        food.setName("Apple");
        food.setBrand("FruitCo");
        food.setBarcode("12345");
        food.setCategory(FoodCategory.FRUITS);

        NutritionalInfo nutrition = new NutritionalInfo();
        nutrition.setCalories(BigDecimal.valueOf(50));
        nutrition.setProtein(BigDecimal.valueOf(1));
        nutrition.setCarbohydrates(BigDecimal.valueOf(14));
        nutrition.setFats(BigDecimal.valueOf(0));
        food.setNutritionalInfo(nutrition);

        when(foodRepository.findAll()).thenReturn(List.of(food));

        elasticSyncService.syncAllFoodsOnStartup();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ElasticFoodDocument>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(elasticFoodRepository).saveAll(listCaptor.capture());

        List<ElasticFoodDocument> savedDocs = listCaptor.getValue();
        assertThat(savedDocs).hasSize(1);
        ElasticFoodDocument doc = savedDocs.get(0);
        assertThat(doc.getId()).isEqualTo("1");
        assertThat(doc.getName()).isEqualTo("Apple");
        assertThat(doc.getBrand()).isEqualTo("FruitCo");
        assertThat(doc.getBarcode()).isEqualTo("12345");
        assertThat(doc.getCategory()).isEqualTo(FoodCategory.FRUITS.name());
        assertThat(doc.getCalories()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(doc.getProtein()).isEqualByComparingTo(BigDecimal.valueOf(1));
    }
}
