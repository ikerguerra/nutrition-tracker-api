package com.nutritiontracker.modules.achievement.dto;

import com.nutritiontracker.modules.achievement.entity.Achievement;
import com.nutritiontracker.modules.achievement.enums.AchievementType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AchievementDto {
    private Long id;
    private AchievementType type;
    private String displayName;
    private int progress;
    private int target;
    private int progressPercentage;
    private boolean unlocked;
    private LocalDateTime unlockedAt;

    public static AchievementDto from(Achievement achievement) {
        return AchievementDto.builder()
                .id(achievement.getId())
                .type(achievement.getType())
                .displayName(achievement.getType().getDisplayName())
                .progress(achievement.getProgress())
                .target(achievement.getTarget())
                .progressPercentage(achievement.getProgressPercentage())
                .unlocked(achievement.isUnlocked())
                .unlockedAt(achievement.getUnlockedAt())
                .build();
    }
}
