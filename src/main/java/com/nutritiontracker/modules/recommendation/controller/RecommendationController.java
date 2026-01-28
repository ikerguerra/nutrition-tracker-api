package com.nutritiontracker.modules.recommendation.controller;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.recommendation.dto.DietPlanResponseDto;
import com.nutritiontracker.modules.recommendation.service.DietGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final DietGenerationService dietGenerationService;

    @GetMapping("/daily")
    public ResponseEntity<DietPlanResponseDto> getDailyPlan(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        return dietGenerationService.getLatestPlan(user.getId(), targetDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/daily")
    public ResponseEntity<DietPlanResponseDto> generateDailyPlan(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "false") boolean forceNew) {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        DietPlanResponseDto plan = dietGenerationService.generateOrRegeneratePlan(user.getId(), targetDate, forceNew);
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/{planId}/accept")
    public ResponseEntity<Void> acceptPlan(
            @AuthenticationPrincipal User user,
            @PathVariable Long planId) {

        dietGenerationService.acceptPlan(planId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{planId}/accept-meal")
    public ResponseEntity<Void> acceptMeal(
            @AuthenticationPrincipal User user,
            @PathVariable Long planId,
            @RequestParam com.nutritiontracker.modules.dailylog.enums.MealType mealType) {

        dietGenerationService.acceptMeal(planId, mealType);
        return ResponseEntity.ok().build();
    }
}
