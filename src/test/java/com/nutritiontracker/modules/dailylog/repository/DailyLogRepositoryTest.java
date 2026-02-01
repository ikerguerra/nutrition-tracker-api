package com.nutritiontracker.modules.dailylog.repository;

import com.nutritiontracker.modules.dailylog.entity.DailyLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for DailyLogRepository
 * Tests database queries using H2 in-memory database
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Daily Log Repository Tests")
class DailyLogRepositoryTest {

    @Autowired
    private DailyLogRepository dailyLogRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find daily log by user ID and date")
    void shouldFindDailyLogByUserIdAndDate() {
        // Given
        DailyLog log = createDailyLog(1L, LocalDate.now());
        entityManager.persist(log);
        entityManager.flush();

        // When
        Optional<DailyLog> found = dailyLogRepository.findByUserIdAndDate(1L, LocalDate.now());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
        assertThat(found.get().getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Should return empty when daily log not found")
    void shouldReturnEmptyWhenDailyLogNotFound() {
        // When
        Optional<DailyLog> found = dailyLogRepository.findByUserIdAndDate(999L, LocalDate.now());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find daily logs by user ID and date range")
    void shouldFindDailyLogsByUserIdAndDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now();

        for (int i = 0; i < 6; i++) {
            DailyLog log = createDailyLog(1L, startDate.plusDays(i));
            entityManager.persist(log);
        }
        entityManager.flush();

        // When
        List<DailyLog> found = dailyLogRepository.findByUserIdAndDateBetween(1L, startDate, endDate);

        // Then
        assertThat(found).hasSize(6);
    }

    @Test
    @DisplayName("Should find daily logs with entries")
    void shouldFindDailyLogsWithEntries() {
        // Given
        DailyLog log = createDailyLog(1L, LocalDate.now());
        entityManager.persist(log);
        entityManager.flush();

        // When
        List<DailyLog> found = dailyLogRepository.findByUserIdAndDateWithEntries(1L, LocalDate.now());

        // Then
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should save daily log with totals")
    void shouldSaveDailyLogWithTotals() {
        // Given
        DailyLog log = createDailyLog(1L, LocalDate.now());
        log.setTotalCalories(BigDecimal.valueOf(2000));
        log.setTotalProtein(BigDecimal.valueOf(150));
        log.setTotalCarbs(BigDecimal.valueOf(200));
        log.setTotalFats(BigDecimal.valueOf(70));

        // When
        DailyLog saved = dailyLogRepository.save(log);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTotalCalories()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(saved.getTotalProtein()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    private DailyLog createDailyLog(Long userId, LocalDate date) {
        DailyLog log = new DailyLog();
        log.setUserId(userId);
        log.setDate(date);
        log.setTotalCalories(BigDecimal.ZERO);
        log.setTotalProtein(BigDecimal.ZERO);
        log.setTotalCarbs(BigDecimal.ZERO);
        log.setTotalFats(BigDecimal.ZERO);
        return log;
    }
}
