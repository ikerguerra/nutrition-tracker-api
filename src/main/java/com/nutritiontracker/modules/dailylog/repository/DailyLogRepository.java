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
     * Find daily log by date (and user_id implicitly for now)
     */
    Optional<DailyLog> findByDate(LocalDate date);

    /**
     * Find daily log with meal entries eagerly loaded
     * Returns a List because JOIN FETCH with multiple bags can return multiple rows
     * for the same entity.
     * We will handle picking the unique result in the service.
     */
    @Query("SELECT DISTINCT dl FROM DailyLog dl LEFT JOIN FETCH dl.mealEntries me LEFT JOIN FETCH me.food WHERE dl.date = :date")
    List<DailyLog> findByDateWithEntries(@Param("date") LocalDate date);

    /**
     * Check if log exists for date
     */
    boolean existsByDate(LocalDate date);
}
