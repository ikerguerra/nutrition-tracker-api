package com.nutritiontracker.modules.recommendation.repository;

import com.nutritiontracker.modules.recommendation.entity.DietRecommendation;
import com.nutritiontracker.modules.dailylog.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DietRecommendationRepository extends JpaRepository<DietRecommendation, Long> {

    List<DietRecommendation> findByUserIdAndDate(Long userId, LocalDate date);

    List<DietRecommendation> findByUserIdAndDateAndMealType(Long userId, LocalDate date, MealType mealType);

    void deleteByUserIdAndDate(Long userId, LocalDate date);

    void deleteByUserId(Long userId);
}
