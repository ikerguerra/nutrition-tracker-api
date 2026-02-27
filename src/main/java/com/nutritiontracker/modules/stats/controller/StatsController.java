package com.nutritiontracker.modules.stats.controller;

import com.nutritiontracker.common.dto.ApiResponse;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.stats.dto.*;
import com.nutritiontracker.modules.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Statistics", description = "Statistics and analytics endpoints")
public class StatsController {

    private final StatsService statsService;
    private final com.nutritiontracker.modules.stats.service.RdaService rdaService;

    @GetMapping("/rda")
    @Operation(summary = "Get User RDA", description = "Get Recommended Daily Allowances for the current user based on profile")
    public ResponseEntity<ApiResponse<RdaDto>> getRda(@AuthenticationPrincipal User user) {
        log.info("REST request to get RDA for userId: {}", user.getId());
        RdaDto data = rdaService.getRdaForUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success("RDA retrieved successfully", data));
    }

    @GetMapping("/weight-history")
    @Operation(summary = "Get weight history", description = "Get weight data points with moving average for a date range")
    public ResponseEntity<ApiResponse<List<WeightDataPointDto>>> getWeightHistory(
            @AuthenticationPrincipal User user,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get weight history from {} to {} for userId: {}", startDate, endDate, user.getId());
        List<WeightDataPointDto> data = statsService.getWeightHistory(startDate, endDate, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Weight history retrieved successfully", data));
    }

    @GetMapping("/macro-trends")
    @Operation(summary = "Get macro trends", description = "Get macro nutrient trends over time with goal adherence")
    public ResponseEntity<ApiResponse<List<MacroTrendDataPointDto>>> getMacroTrends(
            @AuthenticationPrincipal User user,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get macro trends from {} to {} for userId: {}", startDate, endDate, user.getId());
        List<MacroTrendDataPointDto> data = statsService.getMacroTrends(startDate, endDate, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Macro trends retrieved successfully", data));
    }

    @GetMapping("/weekly-summary")
    @Operation(summary = "Get weekly summary", description = "Get comparison between current week and previous week")
    public ResponseEntity<ApiResponse<WeeklySummaryDto>> getWeeklySummary(
            @AuthenticationPrincipal User user,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        log.info("REST request to get weekly summary starting from {} for userId: {}", startDate, user.getId());
        WeeklySummaryDto data = statsService.getWeeklySummary(startDate, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Weekly summary retrieved successfully", data));
    }

    @GetMapping("/goal-achievement")
    @Operation(summary = "Get goal achievement stats", description = "Get goal achievement statistics including streaks")
    public ResponseEntity<ApiResponse<GoalAchievementDto>> getGoalAchievement(
            @AuthenticationPrincipal User user,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get goal achievement from {} to {} for userId: {}", startDate, endDate, user.getId());
        GoalAchievementDto data = statsService.getGoalAchievement(startDate, endDate, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Goal achievement stats retrieved successfully", data));
    }
}
