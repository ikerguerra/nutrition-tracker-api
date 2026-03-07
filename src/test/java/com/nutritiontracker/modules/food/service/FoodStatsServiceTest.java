package com.nutritiontracker.modules.food.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.dailylog.repository.MealEntryRepository;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.entity.FavoriteFood;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.mapper.FoodMapper;
import com.nutritiontracker.modules.food.repository.FavoriteFoodRepository;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Food Stats Service Unit Tests")
class FoodStatsServiceTest {

    @Mock
    private FavoriteFoodRepository favoriteFoodRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private MealEntryRepository mealEntryRepository;

    @Mock
    private FoodMapper foodMapper;

    @InjectMocks
    private FoodStatsService foodStatsService;

    @Test
    @DisplayName("Should successfully add a new favorite food")
    void shouldAddFavoriteFoodSuccessfully() {
        when(foodRepository.existsById(1L)).thenReturn(true);
        when(favoriteFoodRepository.existsByUserIdAndFoodId(1L, 1L)).thenReturn(false);

        foodStatsService.addFavorite(1L, 1L);

        verify(favoriteFoodRepository).save(any(FavoriteFood.class));
    }

    @Test
    @DisplayName("Should not add favorite food if it is already favorited")
    void shouldSkipAddingIfAlreadyFavorite() {
        when(foodRepository.existsById(1L)).thenReturn(true);
        when(favoriteFoodRepository.existsByUserIdAndFoodId(1L, 1L)).thenReturn(true);

        foodStatsService.addFavorite(1L, 1L);

        verify(favoriteFoodRepository, never()).save(any(FavoriteFood.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when adding a favorite food that doesn't exist")
    void shouldThrowExceptionWhenFavoriteFoodNotFound() {
        when(foodRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> foodStatsService.addFavorite(999L, 1L));

        verify(favoriteFoodRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should remove favorite food successfully")
    void shouldRemoveFavoriteFoodSuccessfully() {
        when(favoriteFoodRepository.existsByUserIdAndFoodId(1L, 1L)).thenReturn(true);

        foodStatsService.removeFavorite(1L, 1L);

        verify(favoriteFoodRepository).deleteByUserIdAndFoodId(1L, 1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when removing a non-existent favorite")
    void shouldThrowExceptionWhenRemovingNonExistentFavorite() {
        when(favoriteFoodRepository.existsByUserIdAndFoodId(1L, 999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> foodStatsService.removeFavorite(999L, 1L));

        verify(favoriteFoodRepository, never()).deleteByUserIdAndFoodId(any(), any());
    }

    @Test
    @DisplayName("Should return a list of favorite foods")
    void shouldReturnFavoritesList() {
        FavoriteFood fav1 = FavoriteFood.builder().foodId(10L).userId(1L).build();
        when(favoriteFoodRepository.findByUserId(1L)).thenReturn(List.of(fav1));

        Food food = new Food();
        food.setId(10L);
        when(foodRepository.findAllById(List.of(10L))).thenReturn(List.of(food));

        FoodResponseDto dto = new FoodResponseDto();
        dto.setId(10L);
        when(foodMapper.toDto(food)).thenReturn(dto);

        List<FoodResponseDto> result = foodStatsService.getFavorites(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should return a list of frequent foods")
    void shouldReturnFrequentFoods() {
        when(mealEntryRepository.findTopFrequentFoodIds(eq(1L), any(Pageable.class))).thenReturn(List.of(20L));

        Food food = new Food();
        food.setId(20L);
        when(foodRepository.findAllById(List.of(20L))).thenReturn(List.of(food));

        FoodResponseDto dto = new FoodResponseDto();
        dto.setId(20L);
        when(foodMapper.toDto(food)).thenReturn(dto);

        List<FoodResponseDto> result = foodStatsService.getFrequentFoods(1L, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Should return a list of recent foods")
    void shouldReturnRecentFoods() {
        when(mealEntryRepository.findRecentFoodIds(eq(1L), any(LocalDate.class))).thenReturn(List.of(30L));

        Food food = new Food();
        food.setId(30L);
        when(foodRepository.findAllById(List.of(30L))).thenReturn(List.of(food));

        FoodResponseDto dto = new FoodResponseDto();
        dto.setId(30L);
        when(foodMapper.toDto(food)).thenReturn(dto);

        List<FoodResponseDto> result = foodStatsService.getRecentFoods(1L, 14);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(30L);
    }
}
