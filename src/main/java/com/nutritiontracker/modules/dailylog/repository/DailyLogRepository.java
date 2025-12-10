package com.nutritiontracker.modules.dailylog.repository;

import com.nutritiontracker.modules.dailylog.entity.DailyLog;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    /**
     * Find daily log by user and date
     */
    Optional<DailyLog> findByUserIdAndDate(Long userId, LocalDate date);

    /**
     * Find daily log with meal entries eagerly loaded for a specific user
     */
    @Query("SELECT DISTINCT dl FROM DailyLog dl LEFT JOIN FETCH dl.mealEntries me LEFT JOIN FETCH me.food WHERE dl.userId = :userId AND dl.date = :date")
    List<DailyLog> findByUserIdAndDateWithEntries(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * Find daily logs by user and date range (for calendar)
     */
    List<DailyLog> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Check if log exists for user and date
     */
    boolean existsByUserIdAndDate(Long userId, LocalDate date);
}
