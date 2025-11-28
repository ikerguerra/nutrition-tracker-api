package com.nutritiontracker.modules.dailylog.repository;

import com.nutritiontracker.modules.dailylog.entity.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    /**
     * Find daily log by date (and user_id implicitly for now)
     */
    Optional<DailyLog> findByDate(LocalDate date);

    /**
     * Find daily log with meal entries eagerly loaded
     */
    @Query("SELECT dl FROM DailyLog dl LEFT JOIN FETCH dl.mealEntries me LEFT JOIN FETCH me.food WHERE dl.date = :date")
    Optional<DailyLog> findByDateWithEntries(@Param("date") LocalDate date);

    /**
     * Check if log exists for date
     */
    boolean existsByDate(LocalDate date);
}
