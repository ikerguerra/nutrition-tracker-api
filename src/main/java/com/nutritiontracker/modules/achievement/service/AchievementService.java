package com.nutritiontracker.modules.achievement.service;

import com.nutritiontracker.modules.achievement.dto.AchievementDto;
import com.nutritiontracker.modules.achievement.entity.Achievement;
import com.nutritiontracker.modules.achievement.enums.AchievementType;
import com.nutritiontracker.modules.achievement.repository.AchievementRepository;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    /**
     * Get all achievements for a user. Initializes missing achievements with
     * progress 0.
     */
    @Transactional
    public List<AchievementDto> getAchievementsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Ensure all achievement types exist for this user
        initializeAchievementsForUser(user);

        return achievementRepository.findByUserIdOrderByUnlockedAtDescCreatedAtAsc(userId)
                .stream()
                .map(AchievementDto::from)
                .toList();
    }

    /**
     * Initialize all achievement types for a user if they don't exist yet.
     */
    @Transactional
    public void initializeAchievementsForUser(User user) {
        Arrays.stream(AchievementType.values()).forEach(type -> {
            Optional<Achievement> existing = achievementRepository.findByUserIdAndType(user.getId(), type);
            if (existing.isEmpty()) {
                Achievement achievement = Achievement.builder()
                        .user(user)
                        .type(type)
                        .progress(0)
                        .target(type.getTarget())
                        .build();
                achievementRepository.save(achievement);
                log.debug("Initialized achievement {} for user {}", type, user.getId());
            }
        });
    }

    /**
     * Update progress for a specific achievement type.
     * If target is reached, marks it as unlocked.
     */
    @Transactional
    public void updateProgress(Long userId, AchievementType type, int newProgress) {
        achievementRepository.findByUserIdAndType(userId, type).ifPresent(achievement -> {
            if (achievement.isUnlocked())
                return; // Already unlocked, skip

            achievement.setProgress(Math.min(newProgress, achievement.getTarget()));

            if (achievement.getProgress() >= achievement.getTarget()) {
                achievement.setUnlockedAt(LocalDateTime.now());
                log.info("Achievement UNLOCKED: {} for userId={}", type, userId);
            }

            achievementRepository.save(achievement);
        });
    }

    /**
     * Increment progress by 1 for a specific achievement type.
     */
    @Transactional
    public void incrementProgress(Long userId, AchievementType type) {
        achievementRepository.findByUserIdAndType(userId, type).ifPresent(achievement -> {
            if (achievement.isUnlocked())
                return;

            int newProgress = achievement.getProgress() + 1;
            achievement.setProgress(Math.min(newProgress, achievement.getTarget()));

            if (achievement.getProgress() >= achievement.getTarget()) {
                achievement.setUnlockedAt(LocalDateTime.now());
                log.info("Achievement UNLOCKED: {} for userId={}", type, userId);
            }

            achievementRepository.save(achievement);
        });
    }

    /**
     * Evaluate and update streak-related achievements for a user.
     * Called whenever a daily log is created or updated.
     */
    @Transactional
    public void evaluateStreakAchievements(Long userId, int currentStreak) {
        if (currentStreak >= 3)
            updateProgress(userId, AchievementType.STREAK_3, currentStreak);
        if (currentStreak >= 7)
            updateProgress(userId, AchievementType.STREAK_7, currentStreak);
        if (currentStreak >= 30)
            updateProgress(userId, AchievementType.STREAK_30, currentStreak);
        if (currentStreak >= 100)
            updateProgress(userId, AchievementType.STREAK_100, currentStreak);

        if (currentStreak >= 7)
            updateProgress(userId, AchievementType.CONSISTENCY_WEEK, currentStreak);
        if (currentStreak >= 30)
            updateProgress(userId, AchievementType.CONSISTENCY_MONTH, currentStreak);
    }

    /**
     * Get count of unlocked achievements for a user.
     */
    public long getUnlockedCount(Long userId) {
        return achievementRepository.countByUserIdAndUnlockedAtIsNotNull(userId);
    }
}
