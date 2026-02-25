package com.nutritiontracker.modules.recommendation.repository;

import com.nutritiontracker.modules.recommendation.entity.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    List<DietPlan> findByUserIdAndDateOrderByVersionDesc(Long userId, LocalDate date);

    Optional<DietPlan> findFirstByUserIdAndDateOrderByVersionDesc(Long userId, LocalDate date);

    void deleteByUserId(Long userId);
}
