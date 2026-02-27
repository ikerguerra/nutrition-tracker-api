package com.nutritiontracker.modules.dailylog.controller;

import com.nutritiontracker.common.dto.ApiResponse;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.dailylog.dto.DailyLogResponseDto;
import com.nutritiontracker.modules.dailylog.dto.MealEntryRequestDto;
import com.nutritiontracker.modules.dailylog.enums.MealType;
import com.nutritiontracker.modules.dailylog.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/daily-log")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Daily Log Management", description = "APIs for managing daily nutrition logs and meal entries")
public class DailyLogController {

    private final DailyLogService dailyLogService;

    @GetMapping
    @Operation(summary = "Get daily log", description = "Retrieves the daily log for a specific date. Creates a new one if it doesn't exist.")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> getDailyLog(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Date (YYYY-MM-DD)") @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to get daily log for date: {}", date);
        DailyLogResponseDto dailyLog = dailyLogService.getOrCreateDailyLog(date, user.getId());

        return ResponseEntity.ok(ApiResponse.success(dailyLog));
    }

    @GetMapping("/range")
    @Operation(summary = "Get daily logs by date range", description = "Retrieves daily logs within a specified date range")
    public ResponseEntity<ApiResponse<java.util.List<DailyLogResponseDto>>> getDailyLogsByRange(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Start Date (YYYY-MM-DD)") @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End Date (YYYY-MM-DD)") @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get daily logs for range: {} to {}", startDate, endDate);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        java.util.List<DailyLogResponseDto> logs = dailyLogService.getDailyLogsByDateRange(startDate, endDate,
                user.getId());
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/{date}/breakdown")
    @Operation(summary = "Get nutrient breakdown", description = "Retrieves nutrient breakdown by meal type for a specific date")
    public ResponseEntity<ApiResponse<java.util.List<com.nutritiontracker.modules.dailylog.dto.NutrientBreakdownDto>>> getNutrientBreakdown(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Date (YYYY-MM-DD)") @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to get nutrient breakdown for date: {}", date);
        java.util.List<com.nutritiontracker.modules.dailylog.dto.NutrientBreakdownDto> breakdown = dailyLogService
                .getNutrientBreakdown(date, user.getId());

        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    @PostMapping("/entries")
    @Operation(summary = "Add meal entry", description = "Adds a food item to the daily log")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> addEntry(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MealEntryRequestDto request) {

        log.info("REST request to add meal entry");
        DailyLogResponseDto updatedLog = dailyLogService.addEntry(request, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Entry added successfully", updatedLog));
    }

    @PatchMapping("/{date}/weight")
    @Operation(summary = "Update daily weight", description = "Updates the weight recording for a specific day")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> updateWeight(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Date (YYYY-MM-DD)") @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody com.nutritiontracker.modules.dailylog.dto.DailyLogWeightRequestDto request) {

        log.info("REST request to update weight for date: {}", date);
        DailyLogResponseDto updatedLog = dailyLogService.updateDailyWeight(date, request.getWeight(), user.getId());

        return ResponseEntity.ok(ApiResponse.success("Weight updated successfully", updatedLog));
    }

    @PutMapping("/entries/{id}")
    @Operation(summary = "Update meal entry", description = "Updates an existing meal entry")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> updateEntry(
            @AuthenticationPrincipal User user,
            @Parameter(description = "ID of the entry to update") @PathVariable Long id,
            @Valid @RequestBody MealEntryRequestDto request) {

        log.info("REST request to update meal entry id: {}", id);
        DailyLogResponseDto updatedLog = dailyLogService.updateEntry(id, request, user.getId());

        return ResponseEntity.ok(ApiResponse.success("Entry updated successfully", updatedLog));
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Delete meal entry", description = "Removes a meal entry from the log")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> deleteEntry(
            @AuthenticationPrincipal User user,
            @Parameter(description = "ID of the entry to delete") @PathVariable Long id) {

        log.info("REST request to delete meal entry id: {}", id);
        DailyLogResponseDto updatedLog = dailyLogService.deleteEntry(id, user.getId());

        return ResponseEntity.ok(ApiResponse.success("Entry deleted successfully", updatedLog));
    }

    @PostMapping("/{date}/copy")
    @Operation(summary = "Copy daily log", description = "Copies meal entries from one date to another")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> copyDailyLog(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Source Date (YYYY-MM-DD)") @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Target Date (YYYY-MM-DD)") @RequestParam("targetDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @Parameter(description = "Replace existing entries") @RequestParam(defaultValue = "false") boolean replace) {

        log.info("REST request to copy daily log from {} to {}", date, targetDate);
        DailyLogResponseDto copiedLog = dailyLogService.copyDailyLog(date, targetDate, replace, user.getId());

        return ResponseEntity.ok(ApiResponse.success("Daily log copied successfully", copiedLog));
    }

    @PostMapping("/{date}/meals/{mealType}/copy")
    @Operation(summary = "Copy meal section", description = "Copies all entries of a specific meal type to another date/meal type")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> copyMealSection(
            @AuthenticationPrincipal User user,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String mealType,
            @RequestParam("targetDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @RequestParam String targetMealType,
            @RequestParam(defaultValue = "false") boolean replace) {

        log.info("REST request to copy meal section {} from {} to {}/{}", mealType, date, targetDate, targetMealType);
        DailyLogResponseDto copiedLog = dailyLogService.copyMealSection(
                date,
                MealType.valueOf(mealType),
                targetDate,
                MealType.valueOf(targetMealType),
                replace,
                user.getId());

        return ResponseEntity.ok(ApiResponse.success("Meal section copied successfully", copiedLog));
    }

    @PostMapping("/entries/{id}/copy")
    @Operation(summary = "Copy meal entry", description = "Copies a single meal entry to another date/meal type")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> copyMealEntry(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Parameter(description = "Target Date (YYYY-MM-DD)") @RequestParam("targetDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @Parameter(description = "Target Meal Type") @RequestParam(required = false) String targetMealType) {

        log.info("REST request to copy meal entry {} to date {} and mealType {}", id, targetDate, targetMealType);
        DailyLogResponseDto updatedLog = dailyLogService.copyMealEntry(id, targetDate, targetMealType, user.getId());

        return ResponseEntity.ok(ApiResponse.success("Meal entry copied successfully", updatedLog));
    }
}
