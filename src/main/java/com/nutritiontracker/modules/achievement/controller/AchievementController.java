package com.nutritiontracker.modules.achievement.controller;

import com.nutritiontracker.modules.achievement.dto.AchievementDto;
import com.nutritiontracker.modules.achievement.service.AchievementService;
import com.nutritiontracker.modules.auth.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
@Tag(name = "Achievements", description = "User achievements and gamification")
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    @Operation(summary = "Get all achievements for the current user")
    public ResponseEntity<List<AchievementDto>> getAchievements(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(achievementService.getAchievementsForUser(user.getId()));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get achievements summary (total unlocked count)")
    public ResponseEntity<Map<String, Object>> getAchievementsSummary(
            @AuthenticationPrincipal User user) {
        long unlockedCount = achievementService.getUnlockedCount(user.getId());
        int totalCount = com.nutritiontracker.modules.achievement.enums.AchievementType.values().length;

        return ResponseEntity.ok(Map.of(
                "unlockedCount", unlockedCount,
                "totalCount", totalCount,
                "completionPercentage", totalCount > 0 ? (int) ((double) unlockedCount / totalCount * 100) : 0));
    }
}
