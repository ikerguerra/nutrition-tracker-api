package com.nutritiontracker.modules.recommendation.mapper;

import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.recommendation.dto.*;
import com.nutritiontracker.modules.recommendation.entity.DietPlan;
import com.nutritiontracker.modules.recommendation.entity.DietRecommendation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RecommendationMapper {

        public DietPlanResponseDto toDto(DietPlan plan, NutritionalTotalsDto dailyGoal) {
                if (plan == null)
                        return null;

                Map<com.nutritiontracker.modules.dailylog.enums.MealType, List<DietRecommendation>> grouped = plan
                                .getRecommendations().stream()
                                .collect(Collectors.groupingBy(DietRecommendation::getMealType));

                List<RecommendedMealDto> meals = grouped.entrySet().stream()
                                .map(entry -> {
                                        List<RecommendationItemDto> items = entry.getValue().stream()
                                                        .map(this::toItemDto)
                                                        .collect(Collectors.toList());

                                        return RecommendedMealDto.builder()
                                                        .mealType(entry.getKey())
                                                        .items(items)
                                                        .totals(calculateMealTotals(items))
                                                        .build();
                                })
                                .collect(Collectors.toList());

                BigDecimal totalCal = meals.stream().map(m -> m.getTotals().getCalories()).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                BigDecimal totalProtein = meals.stream().map(m -> m.getTotals().getProtein()).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                BigDecimal totalCarbs = meals.stream().map(m -> m.getTotals().getCarbs()).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                BigDecimal totalFats = meals.stream().map(m -> m.getTotals().getFats()).reduce(BigDecimal.ZERO,
                                BigDecimal::add);

                return DietPlanResponseDto.builder()
                                .id(plan.getId())
                                .date(plan.getDate())
                                .version(plan.getVersion())
                                .status(plan.getStatus())
                                .meals(meals)
                                .planTotals(new NutritionalTotalsDto(totalCal, totalProtein, totalCarbs, totalFats))
                                .dailyGoal(dailyGoal)
                                .build();
        }

        private RecommendationItemDto toItemDto(DietRecommendation rec) {
                // In a real app, you'd fetch the food here or have it hydrated
                // For now, we assume the food info is available or provide it from the service
                return RecommendationItemDto.builder()
                                .id(rec.getId())
                                .foodId(rec.getFoodId())
                                .suggestedQuantity(rec.getSuggestedQuantity())
                                .unit("g") // Defaulting to grams as per context
                                .reason(rec.getReason())
                                .status(rec.getStatus() != null ? rec.getStatus().name() : "PENDING")
                                .build();
        }

        public RecommendationItemDto toItemDto(DietRecommendation rec, Food food) {
                NutritionalInfo nutrition = food.getNutritionalInfo();
                BigDecimal quantity = rec.getSuggestedQuantity();
                BigDecimal servingSize = food.getServingSize() != null ? food.getServingSize()
                                : BigDecimal.valueOf(100);

                // Calculate nutrition for the suggested quantity
                BigDecimal ratio = quantity.divide(servingSize, 4, RoundingMode.HALF_UP);

                NutritionalTotalsDto totals = NutritionalTotalsDto.builder()
                                .calories(safeMultiply(nutrition.getCalories(), ratio))
                                .protein(safeMultiply(nutrition.getProtein(), ratio))
                                .carbs(safeMultiply(nutrition.getCarbohydrates(), ratio))
                                .fats(safeMultiply(nutrition.getFats(), ratio))
                                .build();

                return RecommendationItemDto.builder()
                                .id(rec.getId())
                                .foodId(rec.getFoodId())
                                .foodName(food.getName())
                                .suggestedQuantity(quantity)
                                .unit(food.getServingUnit() != null ? food.getServingUnit() : "g")
                                .reason(rec.getReason())
                                .status(rec.getStatus() != null ? rec.getStatus().name() : "PENDING")
                                .nutritionalInfo(totals)
                                .build();
        }

        private BigDecimal safeMultiply(BigDecimal value, BigDecimal multiplier) {
                if (value == null)
                        return BigDecimal.ZERO;
                return value.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        }

        private NutritionalTotalsDto calculateMealTotals(List<RecommendationItemDto> items) {
                BigDecimal calories = BigDecimal.ZERO;
                BigDecimal protein = BigDecimal.ZERO;
                BigDecimal carbs = BigDecimal.ZERO;
                BigDecimal fats = BigDecimal.ZERO;

                for (RecommendationItemDto item : items) {
                        if (item.getNutritionalInfo() != null) {
                                calories = calories.add(item.getNutritionalInfo().getCalories() != null
                                                ? item.getNutritionalInfo().getCalories()
                                                : BigDecimal.ZERO);
                                protein = protein.add(
                                                item.getNutritionalInfo().getProtein() != null
                                                                ? item.getNutritionalInfo().getProtein()
                                                                : BigDecimal.ZERO);
                                carbs = carbs
                                                .add(item.getNutritionalInfo().getCarbs() != null
                                                                ? item.getNutritionalInfo().getCarbs()
                                                                : BigDecimal.ZERO);
                                fats = fats
                                                .add(item.getNutritionalInfo().getFats() != null
                                                                ? item.getNutritionalInfo().getFats()
                                                                : BigDecimal.ZERO);
                        }
                }

                return new NutritionalTotalsDto(calories, protein, carbs, fats);
        }
}
