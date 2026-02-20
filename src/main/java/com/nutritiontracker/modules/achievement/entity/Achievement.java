package com.nutritiontracker.modules.achievement.entity;

import com.nutritiontracker.modules.achievement.enums.AchievementType;
import com.nutritiontracker.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "achievements", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "type" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AchievementType type;

    @Column(nullable = false)
    @Builder.Default
    private int progress = 0;

    @Column(nullable = false)
    private int target;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public boolean isUnlocked() {
        return unlockedAt != null;
    }

    public int getProgressPercentage() {
        if (target == 0)
            return 100;
        return Math.min(100, (int) ((double) progress / target * 100));
    }
}
