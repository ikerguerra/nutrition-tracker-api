package com.nutritiontracker.modules.dailylog.service;

import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.repository.UserProfileRepository;
import com.nutritiontracker.modules.dailylog.dto.CalendarDayDto;
import com.nutritiontracker.modules.dailylog.entity.DailyLog;
import com.nutritiontracker.modules.dailylog.repository.DailyLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CalendarService {

    private final DailyLogRepository dailyLogRepository;
    private final UserProfileRepository userProfileRepository;

    public List<CalendarDayDto> getMonthlySummary(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        log.info("Fetching calendar summary for user: {} from {} to {}", userId, startDate, endDate);

        List<DailyLog> logs = dailyLogRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        // Get user profile for goals
        BigDecimal calorieGoal = userProfileRepository.findByUserId(userId)
                .map(UserProfile::getDailyCalorieGoal)
                .orElse(new BigDecimal("2000"));

        return logs.stream()
                .map(log -> mapToCalendarDayDto(log, calorieGoal))
                .collect(Collectors.toList());
    }

    private CalendarDayDto mapToCalendarDayDto(DailyLog log, BigDecimal calorieGoal) {
        boolean isGoalMet = false;
        if (log.getTotalCalories() != null && calorieGoal != null) {
            // Simple goal check: within 10% range? Or just < goal?
            // For now let's say "met" if within 100 kcal of goal
            BigDecimal diff = log.getTotalCalories().subtract(calorieGoal).abs();
            isGoalMet = diff.compareTo(new BigDecimal("100")) <= 0;
        }

        return CalendarDayDto.builder()
                .date(log.getDate())
                .totalCalories(log.getTotalCalories())
                .totalProtein(log.getTotalProtein())
                .totalCarbs(log.getTotalCarbs())
                .totalFats(log.getTotalFats())
                .calorieGoal(calorieGoal)
                .isGoalMet(isGoalMet)
                .build();
    }
}
