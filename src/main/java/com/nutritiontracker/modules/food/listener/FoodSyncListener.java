package com.nutritiontracker.modules.food.listener;

import com.nutritiontracker.modules.food.entity.ElasticFoodDocument;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.repository.ElasticFoodRepository;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FoodSyncListener {

    private static ElasticFoodRepository elasticFoodRepository;

    // Static injection workaround for JPA EntityListeners
    @Autowired
    public void setElasticFoodRepository(ElasticFoodRepository repository) {
        FoodSyncListener.elasticFoodRepository = repository;
    }

    @PostPersist
    @PostUpdate
    public void onPostUpdate(Food food) {
        if (elasticFoodRepository == null)
            return;
        elasticFoodRepository.save(convertToElasticDocument(food));
    }

    @PostRemove
    public void onPostRemove(Food food) {
        if (elasticFoodRepository == null)
            return;
        elasticFoodRepository.deleteById(String.valueOf(food.getId()));
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
