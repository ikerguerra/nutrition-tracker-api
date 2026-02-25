package com.nutritiontracker.modules.mealtemplate.repository;

import com.nutritiontracker.modules.mealtemplate.entity.MealTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealTemplateRepository extends JpaRepository<MealTemplate, Long> {

    @Query("SELECT DISTINCT mt FROM MealTemplate mt LEFT JOIN FETCH mt.items i LEFT JOIN FETCH i.food f LEFT JOIN FETCH f.nutritionalInfo WHERE mt.userId = :userId OR mt.isSystem = true")
    List<MealTemplate> findByUserIdOrSystemTrue(@Param("userId") Long userId);

    @Query("SELECT mt FROM MealTemplate mt LEFT JOIN FETCH mt.items i LEFT JOIN FETCH i.food f LEFT JOIN FETCH f.nutritionalInfo WHERE mt.id = :id")
    Optional<MealTemplate> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT mt FROM MealTemplate mt WHERE mt.isPublic = true OR mt.userId = :userId OR mt.isSystem = true")
    List<MealTemplate> findPublicOrUserOrSystemTemplates(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
