package com.nutritiontracker.modules.recommendation.service;

import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.repository.UserProfileRepository;
import com.nutritiontracker.modules.dailylog.enums.MealType;
import com.nutritiontracker.modules.dailylog.service.DailyLogService;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import com.nutritiontracker.modules.recommendation.dto.DietPlanResponseDto;
import com.nutritiontracker.modules.recommendation.dto.NutritionalTotalsDto;
import com.nutritiontracker.modules.recommendation.dto.RecommendationItemDto;
import com.nutritiontracker.modules.recommendation.dto.RecommendedMealDto;
import com.nutritiontracker.modules.recommendation.entity.DietPlan;
import com.nutritiontracker.modules.recommendation.entity.DietRecommendation;
import com.nutritiontracker.modules.recommendation.mapper.RecommendationMapper;
import com.nutritiontracker.modules.recommendation.repository.DietPlanRepository;
import com.nutritiontracker.modules.recommendation.repository.DietRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietGenerationService {

    private final DietAnalysisService dietAnalysisService;
    private final UserProfileRepository userProfileRepository;
    private final DietPlanRepository dietPlanRepository;
    private final DailyLogService dailyLogService;
    private final FoodRepository foodRepository;
    private final RecommendationMapper recommendationMapper;
    private final RecommendationAlgorithmService recommendationAlgorithmService;

    private static final int DAYS_TO_ANALYZE_PATTERNS = 30;
    private static final int FREQUENT_FOODS_LIMIT = 20;

    @Transactional(readOnly = true)
    public Optional<DietPlanResponseDto> getLatestPlan(Long userId, LocalDate date) {
        UserProfile profile = getUserProfile(userId);
        NutritionalTotalsDto dailyGoal = calculateDailyGoalDto(profile);

        return dietPlanRepository.findFirstByUserIdAndDateOrderByVersionDesc(userId, date)
                .map(plan -> hydrateAndMap(plan, dailyGoal));
    }

    @Transactional
    public DietPlanResponseDto generateOrRegeneratePlan(Long userId, LocalDate date, boolean forceNew) {
        UserProfile profile = getUserProfile(userId);
        NutritionalTotalsDto dailyGoal = calculateDailyGoalDto(profile);

        Optional<DietPlan> existingPlan = dietPlanRepository.findFirstByUserIdAndDateOrderByVersionDesc(userId, date);

        if (existingPlan.isPresent() && !forceNew
                && existingPlan.get().getStatus() != DietPlan.DietPlanStatus.DISCARDED) {
            return hydrateAndMap(existingPlan.get(), dailyGoal);
        }

        int nextVersion = existingPlan.map(p -> p.getVersion() + 1).orElse(1);

        // If forceNew, we might want to discard the old ones
        if (forceNew && existingPlan.isPresent()) {
            existingPlan.get().setStatus(DietPlan.DietPlanStatus.DISCARDED);
            dietPlanRepository.save(existingPlan.get());
        }

        DietPlan newPlan = DietPlan.builder()
                .userId(userId)
                .date(date)
                .version(nextVersion)
                .status(DietPlan.DietPlanStatus.GENERATED)
                .build();

        // Algorithm Logic
        generateRecommendationsForPlan(newPlan, profile, date);

        DietPlan savedPlan = dietPlanRepository.saveAndFlush(newPlan);
        return hydrateAndMap(savedPlan, dailyGoal);
    }

    private void generateRecommendationsForPlan(DietPlan plan, UserProfile profile, LocalDate date) {
        MacroTargets dailyTargets = calculateDailyTargets(profile);

        Map<MealType, BigDecimal> distribution = Map.of(
                MealType.BREAKFAST, BigDecimal.valueOf(0.2),
                MealType.LUNCH, BigDecimal.valueOf(0.4),
                MealType.DINNER, BigDecimal.valueOf(0.3),
                MealType.SNACK, BigDecimal.valueOf(0.1));

        Map<MealType, List<Food>> frequentFoodsMap = dietAnalysisService.analyzeFrequentFoods(plan.getUserId(),
                DAYS_TO_ANALYZE_PATTERNS, FREQUENT_FOODS_LIMIT);

        Set<Long> usedFoodIds = new HashSet<>();
        RecommendationAlgorithmService.MacroTotals accumulatedDaily = new RecommendationAlgorithmService.MacroTotals();

        for (MealType mealType : MealType.values()) {
            BigDecimal mealFactor = distribution.getOrDefault(mealType, BigDecimal.valueOf(0.25));

            RecommendationAlgorithmService.MacroTargets mealTarget = new RecommendationAlgorithmService.MacroTargets(
                    dailyTargets.calories.multiply(mealFactor),
                    dailyTargets.protein.multiply(mealFactor),
                    dailyTargets.carbs.multiply(mealFactor),
                    dailyTargets.fats.multiply(mealFactor),
                    dailyTargets.protein);

            List<Food> candidates = new ArrayList<>(frequentFoodsMap.getOrDefault(mealType, new ArrayList<>()));
            if (candidates.size() < 12) {
                candidates.addAll(getFallbackFoods());
            }

            List<DietRecommendation> recs = recommendationAlgorithmService.buildBalancedMeal(plan, mealType,
                    mealTarget, candidates, usedFoodIds, accumulatedDaily);
            recs.forEach(rec -> {
                plan.addRecommendation(rec);
                usedFoodIds.add(rec.getFoodId());
                updateAccumulatedTotals(accumulatedDaily, rec);
            });
        }
    }

    private void updateAccumulatedTotals(RecommendationAlgorithmService.MacroTotals totals, DietRecommendation rec) {
        foodRepository.findById(rec.getFoodId()).ifPresent(food -> {
            NutritionalInfo n = food.getNutritionalInfo();
            if (n != null) {
                BigDecimal servingSize = food.getServingSize() != null ? food.getServingSize()
                        : BigDecimal.valueOf(100);
                BigDecimal ratio = rec.getSuggestedQuantity().divide(servingSize, 4, RoundingMode.HALF_UP);

                totals.protein = totals.protein.add(safe(n.getProtein()).multiply(ratio));
                totals.carbs = totals.carbs.add(safe(n.getCarbohydrates()).multiply(ratio));
                totals.fats = totals.fats.add(safe(n.getFats()).multiply(ratio));
                totals.calories = totals.calories.add(safe(n.getCalories()).multiply(ratio));
            }
        });
    }

    @Transactional
    public void acceptPlan(Long planId) {
        DietPlan plan = dietPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (plan.getStatus() == DietPlan.DietPlanStatus.ACCEPTED)
            return;

        for (DietRecommendation rec : plan.getRecommendations()) {
            acceptRecommendation(rec);
        }

        plan.setStatus(DietPlan.DietPlanStatus.ACCEPTED);
        dietPlanRepository.save(plan);
    }

    private void acceptRecommendation(DietRecommendation recommendation) {
        com.nutritiontracker.modules.dailylog.dto.MealEntryRequestDto request = new com.nutritiontracker.modules.dailylog.dto.MealEntryRequestDto();
        request.setDate(recommendation.getDate());
        request.setMealType(recommendation.getMealType());
        request.setFoodId(recommendation.getFoodId());
        request.setQuantity(recommendation.getSuggestedQuantity());

        Food food = foodRepository.findById(recommendation.getFoodId()).orElse(null);
        request.setUnit(food != null && food.getServingUnit() != null ? food.getServingUnit() : "g");

        dailyLogService.addEntry(request, recommendation.getUserId());
        recommendation.setStatus(DietRecommendation.RecommendationStatus.ACCEPTED);
    }

    private DietPlanResponseDto hydrateAndMap(DietPlan plan, NutritionalTotalsDto dailyGoal) {
        // Hydrate foods for mapping (in a real app, use a join fetch or BatchSize)
        Map<Long, Food> foodCache = new HashMap<>();
        plan.getRecommendations().forEach(rec -> {
            foodCache.computeIfAbsent(rec.getFoodId(), id -> foodRepository.findById(id).orElse(null));
        });

        // The mapper needs the foods to calculate precision
        // We'll use a modified mapper call or handle it here
        DietPlanResponseDto dto = recommendationMapper.toDto(plan, dailyGoal);

        // Enrich items with food names and specific nutrition
        dto.getMeals().forEach(meal -> {
            meal.getItems().forEach(item -> {
                Food f = foodCache.get(item.getFoodId());
                if (f != null) {
                    // Safety: find the matching recommendation entity for this DTO item
                    plan.getRecommendations().stream()
                            .filter(r -> r.getId() != null && r.getId().equals(item.getRecommendationId()))
                            .findFirst()
                            .ifPresent(recEntity -> {
                                RecommendationItemDto enriched = recommendationMapper.toItemDto(recEntity, f);
                                item.setFoodName(enriched.getFoodName());
                                item.setNutritionPerServing(enriched.getNutritionPerServing());
                                item.setUnit(enriched.getUnit());
                            });
                }
            });
            // Recalculate meal totals with enriched items
            meal.setTotals(calculateTotals(meal.getItems()));
        });

        // Recalculate plan totals
        dto.setPlanTotals(calculatePlanTotals(dto.getMeals()));

        return dto;
    }

    private NutritionalTotalsDto calculateTotals(List<RecommendationItemDto> items) {
        BigDecimal c = BigDecimal.ZERO, p = BigDecimal.ZERO, ch = BigDecimal.ZERO, f = BigDecimal.ZERO;
        for (RecommendationItemDto i : items) {
            if (i.getNutritionPerServing() != null) {
                c = c.add(safe(i.getNutritionPerServing().getCalories()));
                p = p.add(safe(i.getNutritionPerServing().getProtein()));
                ch = ch.add(safe(i.getNutritionPerServing().getCarbs()));
                f = f.add(safe(i.getNutritionPerServing().getFats()));
            }
        }
        return new NutritionalTotalsDto(c, p, ch, f);
    }

    private NutritionalTotalsDto calculatePlanTotals(List<RecommendedMealDto> meals) {
        BigDecimal c = BigDecimal.ZERO, p = BigDecimal.ZERO, ch = BigDecimal.ZERO, f = BigDecimal.ZERO;
        for (RecommendedMealDto m : meals) {
            c = c.add(safe(m.getTotals().getCalories()));
            p = p.add(safe(m.getTotals().getProtein()));
            ch = ch.add(safe(m.getTotals().getCarbs()));
            f = f.add(safe(m.getTotals().getFats()));
        }
        return new NutritionalTotalsDto(c, p, ch, f);
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private UserProfile getUserProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

    private NutritionalTotalsDto calculateDailyGoalDto(UserProfile profile) {
        return NutritionalTotalsDto.builder()
                .calories(profile.getDailyCalorieGoal())
                .protein(profile.getDailyProteinGoal())
                .carbs(profile.getDailyCarbsGoal())
                .fats(profile.getDailyFatsGoal())
                .build();
    }

    private MacroTargets calculateDailyTargets(UserProfile profile) {
        return new MacroTargets(
                safe(profile.getDailyCalorieGoal()),
                safe(profile.getDailyProteinGoal()),
                safe(profile.getDailyCarbsGoal()),
                safe(profile.getDailyFatsGoal()));
    }

    private List<Food> getFallbackFoods() {
        return foodRepository.findAll(PageRequest.of(0, 50)).getContent();
    }

    private static class MacroTargets {
        BigDecimal calories;
        BigDecimal protein;
        BigDecimal carbs;
        BigDecimal fats;

        public MacroTargets(BigDecimal c, BigDecimal p, BigDecimal ch, BigDecimal f) {
            this.calories = c;
            this.protein = p;
            this.carbs = ch;
            this.fats = f;
        }
    }
}
