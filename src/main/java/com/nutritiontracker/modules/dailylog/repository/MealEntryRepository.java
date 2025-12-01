package com.nutritiontracker.modules.dailylog.repository;

import com.nutritiontracker.modules.dailylog.entity.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    /**
     * Find meal entry with food and dailyLog eagerly loaded
     */
    @Query("SELECT me FROM MealEntry me JOIN FETCH me.food f JOIN FETCH f.nutritionalInfo JOIN FETCH me.dailyLog WHERE me.id = :id")
    Optional<MealEntry> findByIdWithRelations(@Param("id") Long id);
}
