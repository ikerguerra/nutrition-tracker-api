package com.nutritiontracker.modules.dailylog.service;

import com.nutritiontracker.common.exception.ResourceNotFoundException;
import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.repository.UserProfileRepository;
import com.nutritiontracker.modules.dailylog.dto.DailyLogResponseDto;
import com.nutritiontracker.modules.dailylog.dto.MealEntryRequestDto;
import com.nutritiontracker.modules.dailylog.entity.DailyLog;
import com.nutritiontracker.modules.dailylog.entity.MealEntry;
import com.nutritiontracker.modules.dailylog.enums.MealType;
import com.nutritiontracker.modules.dailylog.repository.DailyLogRepository;
import com.nutritiontracker.modules.dailylog.repository.MealEntryRepository;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import com.nutritiontracker.modules.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DailyLogService {

    private final DailyLogRepository dailyLogRepository;
    private final MealEntryRepository mealEntryRepository;
    private final FoodRepository foodRepository;
    private final UserProfileRepository userProfileRepository;

    /**
     * Get daily log for a specific date. Creates one if it doesn't exist.
     */
    @Transactional
    public DailyLogResponseDto getOrCreateDailyLog(LocalDate date, Long userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateWithEntries(userId, date);
        DailyLog dailyLog;

        if (logs.isEmpty()) {
            dailyLog = createEmptyLog(date, userId);
        } else {
            dailyLog = logs.get(0);
        }

        // Explicitly initialize nutritionalInfo for all entries to avoid lazy loading
        // issues
        for (MealEntry entry : dailyLog.getMealEntries()) {
            entry.getFood().getNutritionalInfo().getCalories(); // Force initialization
        }

        return mapToDto(dailyLog, userId);
    }

    /**
     * Get daily logs for a specific date range
     */
    @Transactional(readOnly = true)
    public List<DailyLogResponseDto> getDailyLogsByDateRange(LocalDate startDate, LocalDate endDate, Long userId) {
        log.info("Getting daily logs for date range: {} to {} and userId: {}", startDate, endDate, userId);
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        List<DailyLogResponseDto> response = new ArrayList<>();
        for (DailyLog dailyLog : logs) {
            // Force initialization similar to getOrCreateDailyLog if needed,
            // but mapToDto access getters which might trigger lazy loading if session is
            // open.
            // Since we are in @Transactional, it should be fine.
            // But safe to force init if mapToDto doesn't access deeply nested lazy props
            // that aren't fetched.
            // findByUserIdAndDateBetween does NOT fetch entries by default in the repo
            // definition I saw?
            // Wait, line 32 in Repo: List<DailyLog> findByUserIdAndDateBetween(...)
            // It uses default JPA method which does NOT eagerly fetch.
            // mapToDto iterates over mealEntries: available in line 240: for (MealEntry
            // entry : log.getMealEntries())
            // This will trigger lazy loading. Since we are in
            // @Transactional(readOnly=true), it should work.
            response.add(mapToDto(dailyLog, userId));
        }
        return response;
    }

    /**
     * Get nutrient breakdown by meal type
     */
    @Transactional(readOnly = true)
    public List<com.nutritiontracker.modules.dailylog.dto.NutrientBreakdownDto> getNutrientBreakdown(LocalDate date,
            Long userId) {
        log.info("Getting nutrient breakdown for date: {} and userId: {}", date, userId);

        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateWithEntries(userId, date);
        if (logs.isEmpty()) {
            return new ArrayList<>();
        }
        DailyLog dailyLog = logs.get(0);

        Map<MealType, com.nutritiontracker.modules.dailylog.dto.NutrientBreakdownDto> aggregates = new EnumMap<>(
                MealType.class);
        for (MealType type : MealType.values()) {
            aggregates.put(type, com.nutritiontracker.modules.dailylog.dto.NutrientBreakdownDto.builder()
                    .mealType(type)
                    .calories(BigDecimal.ZERO)
                    .protein(BigDecimal.ZERO)
                    .carbs(BigDecimal.ZERO)
                    .fats(BigDecimal.ZERO)
                    .fiber(BigDecimal.ZERO)
                    .sugars(BigDecimal.ZERO)
                    .saturatedFats(BigDecimal.ZERO)
                    .sodium(BigDecimal.ZERO)
                    .calcium(BigDecimal.ZERO)
                    .iron(BigDecimal.ZERO)
                    .potassium(BigDecimal.ZERO)
                    .vitaminA(BigDecimal.ZERO)
                    .vitaminC(BigDecimal.ZERO)
                    .vitaminD(BigDecimal.ZERO)
                    .vitaminE(BigDecimal.ZERO)
                    .vitaminB12(BigDecimal.ZERO)
                    .magnesium(BigDecimal.ZERO)
                    .zinc(BigDecimal.ZERO)
                    .vitaminK(BigDecimal.ZERO)
                    .vitaminB1(BigDecimal.ZERO)
                    .vitaminB2(BigDecimal.ZERO)
                    .vitaminB3(BigDecimal.ZERO)
                    .vitaminB6(BigDecimal.ZERO)
                    .vitaminB9(BigDecimal.ZERO)
                    .build());
        }

        BigDecimal dayTotalCalories = BigDecimal.ZERO;
        BigDecimal dayTotalProtein = BigDecimal.ZERO;
        BigDecimal dayTotalCarbs = BigDecimal.ZERO;
        BigDecimal dayTotalFats = BigDecimal.ZERO;

        for (MealEntry entry : dailyLog.getMealEntries()) {
            var dto = aggregates.get(entry.getMealType());

            // Macros are already calculated in MealEntry
            dto.setCalories(dto.getCalories().add(safe(entry.getCalories())));
            dto.setProtein(dto.getProtein().add(safe(entry.getProtein())));
            dto.setCarbs(dto.getCarbs().add(safe(entry.getCarbohydrates())));
            dto.setFats(dto.getFats().add(safe(entry.getFats())));

            dayTotalCalories = dayTotalCalories.add(safe(entry.getCalories()));
            dayTotalProtein = dayTotalProtein.add(safe(entry.getProtein()));
            dayTotalCarbs = dayTotalCarbs.add(safe(entry.getCarbohydrates()));
            dayTotalFats = dayTotalFats.add(safe(entry.getFats()));

            // Helper to calculate micronutrients from Food reference
            Food food = entry.getFood();
            if (food != null && food.getNutritionalInfo() != null) {
                NutritionalInfo info = food.getNutritionalInfo();
                BigDecimal servingSize = food.getServingSize() != null ? food.getServingSize()
                        : BigDecimal.valueOf(100);
                BigDecimal quantity = entry.getQuantity();

                BigDecimal ratio = quantity.divide(servingSize, 4, RoundingMode.HALF_UP);

                dto.setFiber(dto.getFiber().add(safe(info.getFiber()).multiply(ratio)));
                dto.setSugars(dto.getSugars().add(safe(info.getSugars()).multiply(ratio)));
                dto.setSaturatedFats(dto.getSaturatedFats().add(safe(info.getSaturatedFats()).multiply(ratio)));
                dto.setSodium(dto.getSodium().add(safe(info.getSodium()).multiply(ratio)));
                dto.setCalcium(dto.getCalcium().add(safe(info.getCalcium()).multiply(ratio)));
                dto.setIron(dto.getIron().add(safe(info.getIron()).multiply(ratio)));
                dto.setPotassium(dto.getPotassium().add(safe(info.getPotassium()).multiply(ratio)));
                dto.setVitaminA(dto.getVitaminA().add(safe(info.getVitaminA()).multiply(ratio)));
                dto.setVitaminC(dto.getVitaminC().add(safe(info.getVitaminC()).multiply(ratio)));
                dto.setVitaminD(dto.getVitaminD().add(safe(info.getVitaminD()).multiply(ratio)));
                dto.setVitaminE(dto.getVitaminE().add(safe(info.getVitaminE()).multiply(ratio)));
                dto.setVitaminB12(dto.getVitaminB12().add(safe(info.getVitaminB12()).multiply(ratio)));
                dto.setMagnesium(dto.getMagnesium().add(safe(info.getMagnesium()).multiply(ratio)));
                dto.setZinc(dto.getZinc().add(safe(info.getZinc()).multiply(ratio)));
                dto.setVitaminK(dto.getVitaminK().add(safe(info.getVitaminK()).multiply(ratio)));
                dto.setVitaminB1(dto.getVitaminB1().add(safe(info.getVitaminB1()).multiply(ratio)));
                dto.setVitaminB2(dto.getVitaminB2().add(safe(info.getVitaminB2()).multiply(ratio)));
                dto.setVitaminB3(dto.getVitaminB3().add(safe(info.getVitaminB3()).multiply(ratio)));
                dto.setVitaminB6(dto.getVitaminB6().add(safe(info.getVitaminB6()).multiply(ratio)));
                dto.setVitaminB9(dto.getVitaminB9().add(safe(info.getVitaminB9()).multiply(ratio)));
            }
        }

        List<com.nutritiontracker.modules.dailylog.dto.NutrientBreakdownDto> result = new ArrayList<>();

        for (MealType type : MealType.values()) {
            var dto = aggregates.get(type);

            if (dayTotalCalories.compareTo(BigDecimal.ZERO) > 0) {
                dto.setCaloriesPercentage(dto.getCalories().divide(dayTotalCalories, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue());
            }
            if (dayTotalProtein.compareTo(BigDecimal.ZERO) > 0) {
                dto.setProteinPercentage(dto.getProtein().divide(dayTotalProtein, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue());
            }
            if (dayTotalCarbs.compareTo(BigDecimal.ZERO) > 0) {
                dto.setCarbsPercentage(dto.getCarbs().divide(dayTotalCarbs, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue());
            }
            if (dayTotalFats.compareTo(BigDecimal.ZERO) > 0) {
                dto.setFatsPercentage(dto.getFats().divide(dayTotalFats, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue());
            }

            // Round aggregated values for clean output
            dto.setFiber(dto.getFiber().setScale(2, RoundingMode.HALF_UP));
            dto.setSugars(dto.getSugars().setScale(2, RoundingMode.HALF_UP));
            dto.setSaturatedFats(dto.getSaturatedFats().setScale(2, RoundingMode.HALF_UP));
            dto.setSodium(dto.getSodium().setScale(2, RoundingMode.HALF_UP));
            dto.setCalcium(dto.getCalcium().setScale(2, RoundingMode.HALF_UP));
            dto.setIron(dto.getIron().setScale(2, RoundingMode.HALF_UP));
            dto.setPotassium(dto.getPotassium().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminA(dto.getVitaminA().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminC(dto.getVitaminC().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminD(dto.getVitaminD().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminE(dto.getVitaminE().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminB12(dto.getVitaminB12().setScale(2, RoundingMode.HALF_UP));
            dto.setMagnesium(dto.getMagnesium().setScale(2, RoundingMode.HALF_UP));
            dto.setZinc(dto.getZinc().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminK(dto.getVitaminK().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminB1(dto.getVitaminB1().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminB2(dto.getVitaminB2().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminB3(dto.getVitaminB3().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminB6(dto.getVitaminB6().setScale(2, RoundingMode.HALF_UP));
            dto.setVitaminB9(dto.getVitaminB9().setScale(2, RoundingMode.HALF_UP));

            result.add(dto);
        }

        return result;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * Add a new meal entry to the daily log
     */
    @Transactional
    public DailyLogResponseDto addEntry(MealEntryRequestDto request, Long userId) {
        log.info("Adding meal entry for date: {}", request.getDate());

        log.info("Adding meal entry for date: {}", request.getDate());

        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateWithEntries(userId, request.getDate());
        DailyLog dailyLog;

        if (logs.isEmpty()) {
            dailyLog = createEmptyLog(request.getDate(), userId);
        } else {
            dailyLog = logs.get(0);
        }

        Food food = foodRepository.findByIdWithNutritionalInfo(request.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food", request.getFoodId()));

        MealEntry entry = createMealEntry(dailyLog, food, request);
        dailyLog.addMealEntry(entry);

        recalculateTotals(dailyLog);
        DailyLog savedLog = dailyLogRepository.save(dailyLog);

        return mapToDto(savedLog, userId);
    }

    /**
     * Update an existing meal entry
     */
    @Transactional
    public DailyLogResponseDto updateEntry(Long entryId, MealEntryRequestDto request, Long userId) {
        log.info("Updating meal entry id: {}", entryId);

        MealEntry entry = mealEntryRepository.findByIdWithRelations(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("MealEntry", entryId));

        // Update basic fields
        entry.setQuantity(request.getQuantity());
        entry.setUnit(request.getUnit());
        entry.setMealType(request.getMealType());

        // Recalculate macros based on new quantity (using original food data)
        calculateAndSetMacros(entry, entry.getFood(), request.getQuantity());

        // Update log totals
        DailyLog dailyLog = entry.getDailyLog();
        recalculateTotals(dailyLog);
        dailyLogRepository.save(dailyLog);

        return mapToDto(dailyLog, userId);
    }

    /**
     * Delete a meal entry
     */
    @Transactional
    public DailyLogResponseDto deleteEntry(Long entryId, Long userId) {
        log.info("Deleting meal entry id: {}", entryId);

        MealEntry entry = mealEntryRepository.findByIdWithRelations(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("MealEntry", entryId));

        DailyLog dailyLog = entry.getDailyLog();
        dailyLog.removeMealEntry(entry);

        recalculateTotals(dailyLog);
        dailyLogRepository.save(dailyLog);

        return mapToDto(dailyLog, userId);
    }

    /**
     * Update daily weight
     */
    @Transactional
    public DailyLogResponseDto updateDailyWeight(LocalDate date, BigDecimal weight, Long userId) {
        log.info("Updating daily weight for date: {} and userId: {}", date, userId);
        DailyLog dailyLog = getOrCreateDailyLogEntity(date, userId);
        dailyLog.setDailyWeight(weight);
        dailyLogRepository.save(dailyLog);
        return mapToDto(dailyLog, userId);
    }

    // --- Helper Methods ---

    private DailyLog getOrCreateDailyLogEntity(LocalDate date, Long userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateWithEntries(userId, date);
        if (logs.isEmpty()) {
            return createEmptyLog(date, userId);
        }
        return logs.get(0);
    }

    private DailyLog createEmptyLog(LocalDate date, Long userId) {
        log.info("Creating new daily log for date: {} and userId: {}", date, userId);

        // Fetch current goals for snapshot
        var goals = getUserGoals(userId);

        DailyLog dailyLog = DailyLog.builder()
                .date(date)
                .userId(userId)
                .totalCalories(BigDecimal.ZERO)
                .totalProtein(BigDecimal.ZERO)
                .totalCarbs(BigDecimal.ZERO)
                .totalFats(BigDecimal.ZERO)
                .calorieGoal(goals.getCalorieGoal())
                .proteinGoal(goals.getProteinGoal())
                .carbsGoal(goals.getCarbsGoal())
                .fatsGoal(goals.getFatsGoal())
                .mealEntries(new ArrayList<>())
                .build();
        return dailyLogRepository.save(dailyLog);
    }

    private MealEntry createMealEntry(DailyLog dailyLog, Food food, MealEntryRequestDto request) {
        MealEntry entry = MealEntry.builder()
                .dailyLog(dailyLog)
                .food(food)
                .mealType(request.getMealType())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .build();

        calculateAndSetMacros(entry, food, request.getQuantity());
        return entry;
    }

    private void calculateAndSetMacros(MealEntry entry, Food food, BigDecimal quantity) {
        NutritionalInfo info = food.getNutritionalInfo();
        BigDecimal servingSize = food.getServingSize() != null ? food.getServingSize() : BigDecimal.valueOf(100);

        // Ratio = quantity / servingSize
        // Handle divide by zero
        if (servingSize.compareTo(BigDecimal.ZERO) == 0) {
            servingSize = BigDecimal.valueOf(100);
        }
        BigDecimal ratio = quantity.divide(servingSize, 4, RoundingMode.HALF_UP);

        entry.setCalories(safe(info.getCalories()).multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        entry.setProtein(safe(info.getProtein()).multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        entry.setCarbohydrates(safe(info.getCarbohydrates()).multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        entry.setFats(safe(info.getFats()).multiply(ratio).setScale(2, RoundingMode.HALF_UP));
    }

    private void recalculateTotals(DailyLog log) {
        BigDecimal totalCals = BigDecimal.ZERO;
        BigDecimal totalProt = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFats = BigDecimal.ZERO;

        for (MealEntry entry : log.getMealEntries()) {
            totalCals = totalCals.add(safe(entry.getCalories()));
            totalProt = totalProt.add(safe(entry.getProtein()));
            totalCarbs = totalCarbs.add(safe(entry.getCarbohydrates()));
            totalFats = totalFats.add(safe(entry.getFats()));
        }

        log.setTotalCalories(totalCals);
        log.setTotalProtein(totalProt);
        log.setTotalCarbs(totalCarbs);
        log.setTotalFats(totalFats);
    }

    private DailyLogResponseDto mapToDto(DailyLog log) {
        return mapToDto(log, null);
    }

    private DailyLogResponseDto mapToDto(DailyLog log, Long userId) {
        Map<MealType, List<DailyLogResponseDto.MealEntryDto>> meals = new EnumMap<>(MealType.class);

        // Initialize lists for all meal types
        for (MealType type : MealType.values()) {
            meals.put(type, new ArrayList<>());
        }

        // Group entries by meal type
        for (MealEntry entry : log.getMealEntries()) {
            meals.get(entry.getMealType()).add(mapEntryToDto(entry));
        }

        // Fetch goals: Prioritize log snapshot, fallback to current user goals
        DailyLogResponseDto.DailyGoalsDto goalsDto;
        if (log.getCalorieGoal() != null) {
            goalsDto = DailyLogResponseDto.DailyGoalsDto.builder()
                    .calorieGoal(log.getCalorieGoal())
                    .proteinGoal(log.getProteinGoal())
                    .carbsGoal(log.getCarbsGoal())
                    .fatsGoal(log.getFatsGoal())
                    .build();
        } else if (userId != null) {
            goalsDto = getUserGoals(userId);
        } else {
            goalsDto = getDefaultGoals();
        }

        return DailyLogResponseDto.builder()
                .id(log.getId())
                .date(log.getDate())
                .dailyWeight(log.getDailyWeight())
                .totals(DailyLogResponseDto.DailyTotalsDto.builder()
                        .calories(log.getTotalCalories())
                        .protein(log.getTotalProtein())
                        .carbs(log.getTotalCarbs())
                        .fats(log.getTotalFats())
                        .build())
                .goals(goalsDto)
                .meals(meals)
                .build();
    }

    private DailyLogResponseDto.DailyGoalsDto getUserGoals(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .map(profile -> DailyLogResponseDto.DailyGoalsDto.builder()
                        .calorieGoal(profile.getDailyCalorieGoal())
                        .proteinGoal(profile.getDailyProteinGoal())
                        .carbsGoal(profile.getDailyCarbsGoal())
                        .fatsGoal(profile.getDailyFatsGoal())
                        .build())
                .orElse(getDefaultGoals());
    }

    private DailyLogResponseDto.DailyGoalsDto getDefaultGoals() {
        return DailyLogResponseDto.DailyGoalsDto.builder()
                .calorieGoal(new BigDecimal("2000"))
                .proteinGoal(new BigDecimal("150"))
                .carbsGoal(new BigDecimal("200"))
                .fatsGoal(new BigDecimal("65"))
                .build();
    }

    private DailyLogResponseDto.MealEntryDto mapEntryToDto(MealEntry entry) {
        return DailyLogResponseDto.MealEntryDto.builder()
                .id(entry.getId())
                .foodId(entry.getFood().getId())
                .foodName(entry.getFood().getName())
                .brand(entry.getFood().getBrand())
                .quantity(entry.getQuantity())
                .unit(entry.getUnit())
                .calories(entry.getCalories())
                .protein(entry.getProtein())
                .carbs(entry.getCarbohydrates())
                .fats(entry.getFats())
                .build();
    }

    /**
     * Copy daily log from one date to another
     */
    @Transactional
    public DailyLogResponseDto copyDailyLog(LocalDate sourceDate, LocalDate targetDate, boolean replace, Long userId) {
        log.info("Copying daily log from {} to {} for userId: {} (replace={})", sourceDate, targetDate, userId,
                replace);

        if (sourceDate.equals(targetDate)) {
            throw new IllegalArgumentException("Source and target dates cannot be the same");
        }

        DailyLog sourceLog = dailyLogRepository.findByUserIdAndDateWithEntries(userId, sourceDate)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DailyLog", "date", sourceDate));

        DailyLog targetLog = getOrCreateDailyLogEntity(targetDate, userId);

        if (replace) {
            targetLog.getMealEntries().clear();
        }

        for (MealEntry sourceEntry : sourceLog.getMealEntries()) {
            MealEntryRequestDto request = MealEntryRequestDto.builder()
                    .date(targetDate)
                    .mealType(sourceEntry.getMealType())
                    .foodId(sourceEntry.getFood().getId())
                    .quantity(sourceEntry.getQuantity())
                    .unit(sourceEntry.getUnit())
                    .build();

            MealEntry newEntry = createMealEntry(targetLog, sourceEntry.getFood(), request);
            targetLog.addMealEntry(newEntry);
        }

        recalculateTotals(targetLog);
        DailyLog savedLog = dailyLogRepository.save(targetLog);

        return mapToDto(savedLog, userId);
    }

    /**
     * Copy a single meal entry to another date/meal type
     */
    @Transactional
    public DailyLogResponseDto copyMealEntry(Long entryId, LocalDate targetDate, String targetMealType, Long userId) {
        MealEntry sourceEntry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("MealEntry", entryId));

        // Verify ownership (indirectly via DailyLog checks, or explicit check if
        // needed)
        // Here assuming entry ID is sufficient but robust app should check user
        // ownership.
        if (!sourceEntry.getDailyLog().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to this meal entry");
        }

        DailyLog targetLog = getOrCreateDailyLogEntity(targetDate, userId);

        MealType newMealType = targetMealType != null ? MealType.valueOf(targetMealType) : sourceEntry.getMealType();

        MealEntryRequestDto request = MealEntryRequestDto.builder()
                .date(targetDate)
                .mealType(newMealType)
                .foodId(sourceEntry.getFood().getId())
                .quantity(sourceEntry.getQuantity())
                .unit(sourceEntry.getUnit())
                .build();

        MealEntry newEntry = createMealEntry(targetLog, sourceEntry.getFood(), request);
        targetLog.addMealEntry(newEntry);

        recalculateTotals(targetLog);
        dailyLogRepository.save(targetLog);

        // Return the TARGET log so frontend can update if looking at target date,
        // or just acknowledge success. Usually we return the updated resource.
        return mapToDto(targetLog, userId);
    }

    /**
     * Copy all entries of a specific meal type to another date/meal type
     */
    @Transactional
    public DailyLogResponseDto copyMealSection(LocalDate sourceDate, MealType sourceMealType,
            LocalDate targetDate, MealType targetMealType,
            boolean replace, Long userId) {

        DailyLog sourceLog = dailyLogRepository.findByUserIdAndDateWithEntries(userId, sourceDate)
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DailyLog", "date", sourceDate));

        List<MealEntry> sourceEntries = sourceLog.getMealEntries().stream()
                .filter(e -> e.getMealType() == sourceMealType)
                .toList();

        if (sourceEntries.isEmpty()) {
            throw new ResourceNotFoundException("No entries found for meal type: " + sourceMealType);
        }

        DailyLog targetLog = getOrCreateDailyLogEntity(targetDate, userId);

        if (replace) {
            targetLog.getMealEntries().removeIf(e -> e.getMealType() == targetMealType);
        }

        for (MealEntry sourceEntry : sourceEntries) {
            MealEntryRequestDto request = MealEntryRequestDto.builder()
                    .date(targetDate)
                    .mealType(targetMealType)
                    .foodId(sourceEntry.getFood().getId())
                    .quantity(sourceEntry.getQuantity())
                    .unit(sourceEntry.getUnit())
                    .build();

            MealEntry newEntry = createMealEntry(targetLog, sourceEntry.getFood(), request);
            targetLog.addMealEntry(newEntry);
        }

        recalculateTotals(targetLog);
        DailyLog savedLog = dailyLogRepository.save(targetLog);
        return mapToDto(savedLog, userId);
    }
}
