package com.nutritiontracker.modules.recommendation.service;

import com.nutritiontracker.modules.dailylog.enums.MealType;
import com.nutritiontracker.modules.dailylog.repository.MealEntryRepository;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietAnalysisService {

    private final MealEntryRepository mealEntryRepository;
    private final FoodRepository foodRepository;

    @Transactional(readOnly = true)
    public Map<MealType, List<Food>> analyzeFrequentFoods(Long userId, int daysLookback, int limitPerMeal) {
        LocalDate startDate = LocalDate.now().minusDays(daysLookback);

        return Map.of(
                MealType.BREAKFAST, getFrequentFoodsForMeal(userId, MealType.BREAKFAST, startDate, limitPerMeal),
                MealType.LUNCH, getFrequentFoodsForMeal(userId, MealType.LUNCH, startDate, limitPerMeal),
                MealType.DINNER, getFrequentFoodsForMeal(userId, MealType.DINNER, startDate, limitPerMeal),
                MealType.SNACK, getFrequentFoodsForMeal(userId, MealType.SNACK, startDate, limitPerMeal));
    }

    private List<Food> getFrequentFoodsForMeal(Long userId, MealType mealType, LocalDate startDate, int limit) {
        List<Long> foodIds = mealEntryRepository.findTopFrequentFoodIdsByMealType(
                userId, mealType, startDate, PageRequest.of(0, limit));

        if (foodIds.isEmpty()) {
            return Collections.emptyList();
        }

        return foodRepository.findAllById(foodIds);
    }
}
