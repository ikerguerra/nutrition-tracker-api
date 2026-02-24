package com.nutritiontracker;

import com.nutritiontracker.modules.food.repository.ElasticFoodRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

@SpringBootTest
public class ElasticQueryTest {

    @Autowired
    private com.nutritiontracker.modules.food.service.FoodService foodService;

    @Test
    public void testFuzzySearch() {
        try {
            foodService.searchFoods("manzana", null, null, null, null, null, null, null, null, null,
                    PageRequest.of(0, 10));
            System.out.println("SUCCESS a");
            foodService.searchFoods("manz", null, null, null, null, null, null, null, null, null,
                    PageRequest.of(0, 10));
            System.out.println("SUCCESS b");
            foodService.searchFoods("*", null, null, null, null, null, null, null, null, null, PageRequest.of(0, 10));
            System.out.println("SUCCESS c");
        } catch (Exception e) {
            System.out.println("FAILURE CLASS: " + e.getClass().getName());
            e.printStackTrace();
            if (e.getCause() != null) {
                System.out.println("CAUSE: " + e.getCause().getClass().getName());
                e.getCause().printStackTrace();
                if (e.getCause().getCause() != null) {
                    System.out.println("INNER CAUSE: " + e.getCause().getCause().getClass().getName());
                    e.getCause().getCause().printStackTrace();
                }
            }
        }
    }
}
