package com.nutritiontracker.modules.dailylog.repository;

import com.nutritiontracker.modules.dailylog.entity.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    /**
     * Find meal entry with food and dailyLog eagerly loaded
     */
    @Query("SELECT me FROM MealEntry me JOIN FETCH me.food f JOIN FETCH f.nutritionalInfo JOIN FETCH me.dailyLog WHERE me.id = :id")
    Optional<MealEntry> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT me.food.id FROM MealEntry me WHERE me.dailyLog.userId = :userId GROUP BY me.food.id ORDER BY COUNT(me) DESC")
    List<Long> findTopFrequentFoodIds(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT DISTINCT me.food.id FROM MealEntry me WHERE me.dailyLog.userId = :userId AND me.dailyLog.date >= :startDate ORDER BY me.food.id DESC")
    // Note: Ordering by date in the DISTINCT query is complex in JPQL/SQL standard
    // without subqueries or specific dialect features if not selecting the date.
    // For simplicity, we fetch foodIds. If we want strict "most recent order", we
    // might need a different approach.
    // Let's try a simpler approach: Get all entries in range, then process in
    // service or use a subquery if needed.
    // Better query for recent:
    // "SELECT DISTINCT f.id FROM MealEntry me JOIN me.food f WHERE
    // me.dailyLog.userId = :userId AND me.dailyLog.date >= :startDate"
    // To preserve order of recency is harder with DISTINCT.
    // Let's stick to just fetching recent unique foods, application side sorting
    // might be needed if exact timestamp matters,
    // but here we just want "Recent foods".
    List<Long> findRecentFoodIds(@Param("userId") Long userId, @Param("startDate") java.time.LocalDate startDate);
}
