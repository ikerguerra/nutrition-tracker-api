package com.nutritiontracker.modules.food.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.dailylog.repository.MealEntryRepository;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.entity.FavoriteFood;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.mapper.FoodMapper;
import com.nutritiontracker.modules.food.repository.FavoriteFoodRepository;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FoodStatsService {

    private final FavoriteFoodRepository favoriteFoodRepository;
    private final FoodRepository foodRepository;
    private final MealEntryRepository mealEntryRepository;
    private final FoodMapper foodMapper;

    @Transactional
    public void addFavorite(Long foodId, Long userId) {
        if (!foodRepository.existsById(foodId)) {
            throw new ResourceNotFoundException("Food not found with id: " + foodId);
        }
        if (favoriteFoodRepository.existsByUserIdAndFoodId(userId, foodId)) {
            return; // Already favorite
        }
        FavoriteFood favorite = FavoriteFood.builder()
                .userId(userId)
                .foodId(foodId)
                .build();
        favoriteFoodRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long foodId, Long userId) {
        if (!favoriteFoodRepository.existsByUserIdAndFoodId(userId, foodId)) {
            throw new ResourceNotFoundException("Favorite not found for food id: " + foodId);
        }
        favoriteFoodRepository.deleteByUserIdAndFoodId(userId, foodId);
    }

    public List<FoodResponseDto> getFavorites(Long userId) {
        List<FavoriteFood> favorites = favoriteFoodRepository.findByUserId(userId);
        List<Long> foodIds = favorites.stream().map(FavoriteFood::getFoodId).collect(Collectors.toList());
        List<Food> foods = foodRepository.findAllById(foodIds);
        return foods.stream().map(foodMapper::toDto).collect(Collectors.toList());
    }

    public List<FoodResponseDto> getFrequentFoods(Long userId, int limit) {
        List<Long> foodIds = mealEntryRepository.findTopFrequentFoodIds(userId, PageRequest.of(0, limit));
        List<Food> foods = foodRepository.findAllById(foodIds);

        // Restore order as findAllById doesn't guarantee it
        return foodIds.stream()
                .map(id -> foods.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(foodMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<FoodResponseDto> getRecentFoods(Long userId, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<Long> foodIds = mealEntryRepository.findRecentFoodIds(userId, startDate);
        List<Food> foods = foodRepository.findAllById(foodIds);

        // Simple distinct return
        return foods.stream()
                .map(foodMapper::toDto)
                .collect(Collectors.toList());
    }
}
