package com.nutritiontracker.modules.recommendation.service;

import com.nutritiontracker.modules.dailylog.enums.MealType;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.recommendation.entity.DietPlan;
import com.nutritiontracker.modules.recommendation.entity.DietRecommendation;
import com.nutritiontracker.modules.recommendation.enums.FoodCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationAlgorithmService {

    private final FoodClassificationService foodClassificationService;

    public List<DietRecommendation> buildBalancedMeal(DietPlan plan, MealType mealType, MacroTargets target,
            List<Food> candidates, Set<Long> usedFoodIds, MacroTotals accumulatedDaily) {

        List<DietRecommendation> recommendations = new ArrayList<>();
        if (candidates.isEmpty())
            return recommendations;

        // 1. Hard Protein Cap Check (105%)
        boolean proteinCapReached = accumulatedDaily != null &&
                accumulatedDaily.protein.compareTo(target.dailyProteinTarget.multiply(BigDecimal.valueOf(1.05))) >= 0;

        // 2. Categorize candidates
        Map<FoodCategory, List<Food>> categorized = candidates.stream()
                .filter(f -> !usedFoodIds.contains(f.getId()))
                .collect(Collectors.groupingBy(foodClassificationService::classify));

        // 3. Layered Selection
        Food proteinSource = null;
        if (!proteinCapReached) {
            proteinSource = selectBestSource(categorized.get(FoodCategory.LEAN_PROTEIN));
            if (proteinSource == null) {
                proteinSource = selectBestSource(categorized.get(FoodCategory.FATTY_PROTEIN));
            }
        }

        Food carbSource = selectBestSource(categorized.get(FoodCategory.CARB_COMPLEX));
        Food fatSource = selectBestSource(categorized.get(FoodCategory.FAT));

        // Fallback for carbohydrates (essential for energy)
        if (carbSource == null) {
            carbSource = candidates.get(0);
        }

        // 4. Portion Calculation & Implicit Budgeting
        BigDecimal proteinQty = BigDecimal.ZERO;
        BigDecimal implicitFat = BigDecimal.ZERO;
        boolean isFattyProtein = false;

        if (proteinSource != null) {
            proteinQty = calculateInitialQuantity(proteinSource, target.protein,
                    foodClassificationService.classify(proteinSource));
            implicitFat = getMacroContribution(proteinSource, proteinQty, "fats");
            isFattyProtein = foodClassificationService.classify(proteinSource) == FoodCategory.FATTY_PROTEIN;

            recommendations.add(createRecommendation(plan, mealType, proteinSource, proteinQty,
                    isFattyProtein ? "Proteína base (con grasas naturales)" : "Proteína magra para tus objetivos"));
        }

        // Adjust carb source (Scaling priority)
        BigDecimal remainingCarbs = target.carbs
                .subtract(getMacroContribution(proteinSource, proteinQty, "carbs"))
                .max(BigDecimal.ZERO);

        BigDecimal carbQty = calculateInitialQuantity(carbSource, remainingCarbs, FoodCategory.CARB_COMPLEX);

        // If we still need calories but protein is capped, boost carbs moderately
        if (proteinCapReached && carbQty.compareTo(BigDecimal.ZERO) > 0) {
            carbQty = carbQty.multiply(BigDecimal.valueOf(1.2)).setScale(0, RoundingMode.HALF_UP);
        }

        if (carbQty.compareTo(BigDecimal.ZERO) > 0) {
            recommendations.add(createRecommendation(plan, mealType, carbSource, carbQty,
                    "Carbohidratos complejos para energía sostenida"));
        }

        // Fat Budgeting
        if (fatSource != null && !isFattyProtein) {
            BigDecimal remainingFats = target.fats.subtract(implicitFat).max(BigDecimal.ZERO);
            if (remainingFats.compareTo(BigDecimal.valueOf(5)) > 0) {
                BigDecimal fatQty = calculateInitialQuantity(fatSource, remainingFats, FoodCategory.FAT);
                if (fatQty.compareTo(BigDecimal.valueOf(5)) > 0) {
                    recommendations
                            .add(createRecommendation(plan, mealType, fatSource, fatQty, "Grasas saludables de apoyo"));
                }
            }
        }

        return recommendations;
    }

    private Food selectBestSource(List<Food> foods) {
        if (foods == null || foods.isEmpty())
            return null;
        List<Food> shuffled = new ArrayList<>(foods);
        Collections.shuffle(shuffled);
        return shuffled.get(0);
    }

    private BigDecimal calculateInitialQuantity(Food food, BigDecimal targetMacro, FoodCategory category) {
        if (food == null || targetMacro == null)
            return BigDecimal.ZERO;
        NutritionalInfo nutrition = food.getNutritionalInfo();
        if (nutrition == null)
            return BigDecimal.valueOf(100);

        BigDecimal macroPer100 = getMacroValue(nutrition, category);
        if (macroPer100.compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.valueOf(100);

        BigDecimal servingSize = food.getServingSize() != null ? food.getServingSize() : BigDecimal.valueOf(100);
        BigDecimal quantity = targetMacro.divide(macroPer100, 4, RoundingMode.HALF_UP).multiply(servingSize);

        BigDecimal limit = foodClassificationService.getPortionLimits().getOrDefault(category, BigDecimal.valueOf(300));
        return quantity.setScale(0, RoundingMode.HALF_UP).min(limit).max(BigDecimal.valueOf(10));
    }

    private BigDecimal getMacroValue(NutritionalInfo nutrition, FoodCategory category) {
        return switch (category) {
            case LEAN_PROTEIN, FATTY_PROTEIN ->
                nutrition.getProtein() != null ? nutrition.getProtein() : BigDecimal.ONE;
            case CARB_COMPLEX, CARB_SIMPLE ->
                nutrition.getCarbohydrates() != null ? nutrition.getCarbohydrates() : BigDecimal.ONE;
            case FAT -> nutrition.getFats() != null ? nutrition.getFats() : BigDecimal.ONE;
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal getMacroContribution(Food food, BigDecimal quantity, String macro) {
        if (food == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0)
            return BigDecimal.ZERO;
        NutritionalInfo n = food.getNutritionalInfo();
        if (n == null)
            return BigDecimal.ZERO;
        BigDecimal servingSize = food.getServingSize() != null ? food.getServingSize() : BigDecimal.valueOf(100);
        BigDecimal ratio = quantity.divide(servingSize, 4, RoundingMode.HALF_UP);

        return switch (macro) {
            case "protein" -> n.getProtein() != null ? n.getProtein().multiply(ratio) : BigDecimal.ZERO;
            case "carbs" -> n.getCarbohydrates() != null ? n.getCarbohydrates().multiply(ratio) : BigDecimal.ZERO;
            case "fats" -> n.getFats() != null ? n.getFats().multiply(ratio) : BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    private DietRecommendation createRecommendation(DietPlan plan, MealType mealType, Food food, BigDecimal quantity,
            String reason) {
        return DietRecommendation.builder()
                .userId(plan.getUserId())
                .date(plan.getDate())
                .mealType(mealType)
                .foodId(food.getId())
                .suggestedQuantity(quantity)
                .reason(reason)
                .status(DietRecommendation.RecommendationStatus.PENDING)
                .build();
    }

    public static class MacroTargets {
        public BigDecimal calories;
        public BigDecimal protein;
        public BigDecimal carbs;
        public BigDecimal fats;
        public BigDecimal dailyProteinTarget;

        public MacroTargets(BigDecimal c, BigDecimal p, BigDecimal ch, BigDecimal f, BigDecimal dailyP) {
            this.calories = c;
            this.protein = p;
            this.carbs = ch;
            this.fats = f;
            this.dailyProteinTarget = dailyP;
        }
    }

    public static class MacroTotals {
        public BigDecimal protein = BigDecimal.ZERO;
        public BigDecimal carbs = BigDecimal.ZERO;
        public BigDecimal fats = BigDecimal.ZERO;
        public BigDecimal calories = BigDecimal.ZERO;
    }
}
