package com.nutritiontracker.modules.stats.service;

import com.nutritiontracker.modules.dailylog.entity.DailyLog;
import com.nutritiontracker.modules.dailylog.repository.DailyLogRepository;
import com.nutritiontracker.modules.stats.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatsService {

    private final DailyLogRepository dailyLogRepository;

    /**
     * Get weight history with moving average
     */
    public List<WeightDataPointDto> getWeightHistory(LocalDate startDate, LocalDate endDate, Long userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        // Filter logs with weight data
        List<DailyLog> logsWithWeight = logs.stream()
                .filter(log -> log.getDailyWeight() != null)
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());

        List<WeightDataPointDto> dataPoints = new ArrayList<>();
        Double previousWeight = null;

        for (int i = 0; i < logsWithWeight.size(); i++) {
            DailyLog log = logsWithWeight.get(i);
            Double weight = log.getDailyWeight().doubleValue();
            Double weightChange = previousWeight != null ? weight - previousWeight : 0.0;
            Double movingAvg = calculateMovingAverage(logsWithWeight, i, 7);

            dataPoints.add(WeightDataPointDto.builder()
                    .date(log.getDate())
                    .weight(weight)
                    .weightChange(weightChange)
                    .movingAverage(movingAvg)
                    .build());

            previousWeight = weight;
        }

        return dataPoints;
    }

    /**
     * Get macro trends over time
     */
    public List<MacroTrendDataPointDto> getMacroTrends(LocalDate startDate, LocalDate endDate, Long userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        return logs.stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .map(log -> {
                    Double adherence = calculateAdherence(log);
                    return MacroTrendDataPointDto.builder()
                            .date(log.getDate())
                            .calories(log.getTotalCalories() != null ? log.getTotalCalories().intValue() : 0)
                            .protein(log.getTotalProtein() != null ? log.getTotalProtein().doubleValue() : 0.0)
                            .carbs(log.getTotalCarbs() != null ? log.getTotalCarbs().doubleValue() : 0.0)
                            .fats(log.getTotalFats() != null ? log.getTotalFats().doubleValue() : 0.0)
                            .goalCalories(log.getCalorieGoal() != null ? log.getCalorieGoal().intValue() : 0)
                            .goalProtein(log.getProteinGoal() != null ? log.getProteinGoal().doubleValue() : 0.0)
                            .goalCarbs(log.getCarbsGoal() != null ? log.getCarbsGoal().doubleValue() : 0.0)
                            .goalFats(log.getFatsGoal() != null ? log.getFatsGoal().doubleValue() : 0.0)
                            .adherencePercentage(adherence)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get weekly summary comparison
     */
    public WeeklySummaryDto getWeeklySummary(LocalDate startDate, Long userId) {
        LocalDate currentWeekStart = startDate;
        LocalDate currentWeekEnd = startDate.plusDays(6);
        LocalDate previousWeekStart = startDate.minusDays(7);
        LocalDate previousWeekEnd = startDate.minusDays(1);

        WeekDataDto currentWeek = calculateWeekData(currentWeekStart, currentWeekEnd, userId);
        WeekDataDto previousWeek = calculateWeekData(previousWeekStart, previousWeekEnd, userId);

        WeekComparisonDto changes = WeekComparisonDto.builder()
                .caloriesChange(calculatePercentageChange(previousWeek.getAvgCalories(), currentWeek.getAvgCalories()))
                .proteinChange(calculatePercentageChange(previousWeek.getAvgProtein(), currentWeek.getAvgProtein()))
                .carbsChange(calculatePercentageChange(previousWeek.getAvgCarbs(), currentWeek.getAvgCarbs()))
                .fatsChange(calculatePercentageChange(previousWeek.getAvgFats(), currentWeek.getAvgFats()))
                .build();

        return WeeklySummaryDto.builder()
                .currentWeek(currentWeek)
                .previousWeek(previousWeek)
                .changes(changes)
                .build();
    }

    /**
     * Get goal achievement statistics
     */
    public GoalAchievementDto getGoalAchievement(LocalDate startDate, LocalDate endDate, Long userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        int totalDays = logs.size();
        int achievedDays = 0;
        int currentStreak = 0;
        int bestStreak = 0;
        int tempStreak = 0;

        // Sort by date
        logs = logs.stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());

        for (DailyLog log : logs) {
            boolean achieved = isGoalAchieved(log);
            if (achieved) {
                achievedDays++;
                tempStreak++;
                if (tempStreak > bestStreak) {
                    bestStreak = tempStreak;
                }
            } else {
                tempStreak = 0;
            }
        }

        // Current streak is the temp streak if it's still ongoing
        if (!logs.isEmpty() && isGoalAchieved(logs.get(logs.size() - 1))) {
            currentStreak = tempStreak;
        }

        double achievementRate = totalDays > 0 ? (achievedDays * 100.0 / totalDays) : 0.0;

        return GoalAchievementDto.builder()
                .totalDays(totalDays)
                .achievedDays(achievedDays)
                .achievementRate(achievementRate)
                .currentStreak(currentStreak)
                .bestStreak(bestStreak)
                .build();
    }

    // Helper methods

    private Double calculateMovingAverage(List<DailyLog> logs, int currentIndex, int window) {
        int start = Math.max(0, currentIndex - window + 1);
        int end = currentIndex + 1;

        double sum = 0;
        int count = 0;
        for (int i = start; i < end; i++) {
            BigDecimal weight = logs.get(i).getDailyWeight();
            if (weight != null) {
                sum += weight.doubleValue();
                count++;
            }
        }

        return count > 0 ? sum / count : null;
    }

    private Double calculateAdherence(DailyLog log) {
        if (log.getCalorieGoal() == null || log.getCalorieGoal().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        double totalCals = log.getTotalCalories() != null ? log.getTotalCalories().doubleValue() : 0.0;
        double goalCals = log.getCalorieGoal().doubleValue();
        double calorieAdherence = 100.0 - Math.abs((totalCals - goalCals) / goalCals * 100);
        return Math.max(0, Math.min(100, calorieAdherence));
    }

    private WeekDataDto calculateWeekData(LocalDate start, LocalDate end, Long userId) {
        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateBetween(userId, start, end);

        if (logs.isEmpty()) {
            return WeekDataDto.builder()
                    .avgCalories(0.0)
                    .avgProtein(0.0)
                    .avgCarbs(0.0)
                    .avgFats(0.0)
                    .daysLogged(0)
                    .build();
        }

        double avgCalories = logs.stream()
                .mapToDouble(log -> log.getTotalCalories() != null ? log.getTotalCalories().doubleValue() : 0.0)
                .average().orElse(0.0);
        double avgProtein = logs.stream()
                .mapToDouble(log -> log.getTotalProtein() != null ? log.getTotalProtein().doubleValue() : 0.0)
                .average().orElse(0.0);
        double avgCarbs = logs.stream()
                .mapToDouble(log -> log.getTotalCarbs() != null ? log.getTotalCarbs().doubleValue() : 0.0)
                .average().orElse(0.0);
        double avgFats = logs.stream()
                .mapToDouble(log -> log.getTotalFats() != null ? log.getTotalFats().doubleValue() : 0.0)
                .average().orElse(0.0);

        return WeekDataDto.builder()
                .avgCalories(avgCalories)
                .avgProtein(avgProtein)
                .avgCarbs(avgCarbs)
                .avgFats(avgFats)
                .daysLogged(logs.size())
                .build();
    }

    private Double calculatePercentageChange(Double oldValue, Double newValue) {
        if (oldValue == null || oldValue == 0) {
            return 0.0;
        }
        return ((newValue - oldValue) / oldValue) * 100;
    }

    private boolean isGoalAchieved(DailyLog log) {
        if (log.getCalorieGoal() == null || log.getCalorieGoal().compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        double tolerance = 0.05; // 5% tolerance
        double goalCals = log.getCalorieGoal().doubleValue();
        double totalCals = log.getTotalCalories() != null ? log.getTotalCalories().doubleValue() : 0.0;
        double lowerBound = goalCals * (1 - tolerance);
        double upperBound = goalCals * (1 + tolerance);

        return totalCals >= lowerBound && totalCals <= upperBound;
    }
}
