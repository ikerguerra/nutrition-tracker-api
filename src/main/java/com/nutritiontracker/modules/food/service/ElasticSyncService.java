package com.nutritiontracker.modules.food.service;

import com.nutritiontracker.modules.food.entity.ElasticFoodDocument;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.repository.ElasticFoodRepository;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticSyncService {

    private final FoodRepository foodRepository;
    private final ElasticFoodRepository elasticFoodRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void syncAllFoodsOnStartup() {
        log.info("Starting initial bulk sync of Foods to Elasticsearch...");

        long count = elasticFoodRepository.count();
        if (count > 0) {
            log.info("Elasticsearch already contains {} foods. Skipping startup sync.", count);
            return;
        }

        List<Food> allFoods = foodRepository.findAll();
        if (allFoods.isEmpty()) {
            log.info("No foods found in MySQL database to sync.");
            return;
        }

        List<ElasticFoodDocument> docs = allFoods.stream()
                .map(this::convertToElasticDocument)
                .collect(Collectors.toList());

        elasticFoodRepository.saveAll(docs);
        log.info("Successfully synced {} foods to Elasticsearch.", docs.size());
    }

    private ElasticFoodDocument convertToElasticDocument(Food food) {
        ElasticFoodDocument doc = ElasticFoodDocument.builder()
                .id(String.valueOf(food.getId()))
                .name(food.getName())
                .brand(food.getBrand())
                .barcode(food.getBarcode())
                .category(food.getCategory() != null ? food.getCategory().name() : null)
                .build();

        if (food.getNutritionalInfo() != null) {
            doc.setCalories(food.getNutritionalInfo().getCalories());
            doc.setProtein(food.getNutritionalInfo().getProtein());
            doc.setCarbohydrates(food.getNutritionalInfo().getCarbohydrates());
            doc.setFats(food.getNutritionalInfo().getFats());
        }

        return doc;
    }
}
