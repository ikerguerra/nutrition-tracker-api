package com.nutritiontracker.modules.achievement.repository;

import com.nutritiontracker.modules.achievement.entity.Achievement;
import com.nutritiontracker.modules.achievement.enums.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    @Query("SELECT a FROM Achievement a WHERE a.user.id = :userId ORDER BY a.unlockedAt DESC NULLS LAST, a.createdAt ASC")
    List<Achievement> findByUserIdOrderByUnlockedAtDescCreatedAtAsc(@Param("userId") Long userId);

    Optional<Achievement> findByUserIdAndType(Long userId, AchievementType type);

    long countByUserIdAndUnlockedAtIsNotNull(Long userId);

    void deleteByUserId(Long userId);
}
