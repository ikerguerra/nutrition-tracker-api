package com.nutritiontracker.modules.recommendation.service;

import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.recommendation.enums.FoodCategory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class FoodClassificationService {

    public FoodCategory classify(Food food) {
        NutritionalInfo nutrition = food.getNutritionalInfo();
        if (nutrition == null)
            return FoodCategory.UNKNOWN;

        BigDecimal calories = nutrition.getCalories();
        if (calories == null || calories.compareTo(BigDecimal.ZERO) <= 0) {
            // Fallback: calculate calories from macros
            calories = calculateCalories(nutrition);
        }

        if (calories.compareTo(BigDecimal.ZERO) <= 0)
            return FoodCategory.UNKNOWN;

        BigDecimal p = nutrition.getProtein() != null ? nutrition.getProtein() : BigDecimal.ZERO;
        BigDecimal c = nutrition.getCarbohydrates() != null ? nutrition.getCarbohydrates() : BigDecimal.ZERO;
        BigDecimal f = nutrition.getFats() != null ? nutrition.getFats() : BigDecimal.ZERO;

        BigDecimal proteinCals = p.multiply(BigDecimal.valueOf(4));
        BigDecimal carbCals = c.multiply(BigDecimal.valueOf(4));
        BigDecimal fatCals = f.multiply(BigDecimal.valueOf(9));

        // Ratios
        BigDecimal proteinRatio = proteinCals.divide(calories, 4, RoundingMode.HALF_UP);
        BigDecimal fatRatio = fatCals.divide(calories, 4, RoundingMode.HALF_UP);

        // 1. Protein Layer (Lean vs Fatty)
        if (proteinRatio.compareTo(BigDecimal.valueOf(0.25)) > 0) {
            // If fat ratio is > 25% of calories, it's a fatty protein source
            if (fatRatio.compareTo(BigDecimal.valueOf(0.25)) > 0) {
                return FoodCategory.FATTY_PROTEIN;
            }
            return FoodCategory.LEAN_PROTEIN;
        }

        // 2. Pure Fat Source
        if (fatRatio.compareTo(BigDecimal.valueOf(0.50)) > 0) {
            return FoodCategory.FAT;
        }

        // 3. Carbohydrates: Complex vs Simple
        BigDecimal carbs = nutrition.getCarbohydrates();
        if (carbs != null && carbs.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sugars = nutrition.getSugars() != null ? nutrition.getSugars() : BigDecimal.ZERO;
            BigDecimal fiber = nutrition.getFiber() != null ? nutrition.getFiber() : BigDecimal.ZERO;

            // Simple if sugar > 15% of total carbs
            BigDecimal sugarRatio = sugars.divide(carbs, 4, RoundingMode.HALF_UP);
            if (sugarRatio.compareTo(BigDecimal.valueOf(0.15)) > 0) {
                return FoodCategory.CARB_SIMPLE;
            }

            // Complex if high fiber or just not simple
            return FoodCategory.CARB_COMPLEX;
        }

        return FoodCategory.UNKNOWN;
    }

    public Map<FoodCategory, BigDecimal> getPortionLimits() {
        Map<FoodCategory, BigDecimal> limits = new HashMap<>();
        limits.put(FoodCategory.LEAN_PROTEIN, BigDecimal.valueOf(250));
        limits.put(FoodCategory.FATTY_PROTEIN, BigDecimal.valueOf(180));
        limits.put(FoodCategory.CARB_COMPLEX, BigDecimal.valueOf(350));
        limits.put(FoodCategory.CARB_SIMPLE, BigDecimal.valueOf(50));
        limits.put(FoodCategory.FAT, BigDecimal.valueOf(40));
        limits.put(FoodCategory.UNKNOWN, BigDecimal.valueOf(100));
        return limits;
    }

    private BigDecimal calculateCalories(NutritionalInfo nutrition) {
        BigDecimal p = nutrition.getProtein() != null ? nutrition.getProtein() : BigDecimal.ZERO;
        BigDecimal c = nutrition.getCarbohydrates() != null ? nutrition.getCarbohydrates() : BigDecimal.ZERO;
        BigDecimal f = nutrition.getFats() != null ? nutrition.getFats() : BigDecimal.ZERO;
        return p.multiply(BigDecimal.valueOf(4))
                .add(c.multiply(BigDecimal.valueOf(4)))
                .add(f.multiply(BigDecimal.valueOf(9)));
    }
}
