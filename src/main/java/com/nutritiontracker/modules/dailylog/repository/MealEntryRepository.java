package com.nutritiontracker.modules.dailylog.repository;

import com.nutritiontracker.modules.dailylog.entity.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {
    // Standard CRUD operations are sufficient for now
}
