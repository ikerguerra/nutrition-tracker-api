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
        BigDecimal servingSize = food.getServingSize();

        // Ratio = quantity / servingSize
        BigDecimal ratio = quantity.divide(servingSize, 4, RoundingMode.HALF_UP);

        entry.setCalories(info.getCalories().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        entry.setProtein(info.getProtein().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        entry.setCarbohydrates(info.getCarbohydrates().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
        entry.setFats(info.getFats().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
    }

    private void recalculateTotals(DailyLog log) {
        BigDecimal totalCals = BigDecimal.ZERO;
        BigDecimal totalProt = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFats = BigDecimal.ZERO;

        for (MealEntry entry : log.getMealEntries()) {
            totalCals = totalCals.add(entry.getCalories());
            totalProt = totalProt.add(entry.getProtein());
            totalCarbs = totalCarbs.add(entry.getCarbohydrates());
            totalFats = totalFats.add(entry.getFats());
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
}
