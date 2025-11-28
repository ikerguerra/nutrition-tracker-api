package com.nutritiontracker.modules.dailylog.controller;

import com.nutritiontracker.common.dto.ApiResponse;
import com.nutritiontracker.modules.dailylog.dto.DailyLogResponseDto;
import com.nutritiontracker.modules.dailylog.dto.MealEntryRequestDto;
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
            @Parameter(description = "Date (YYYY-MM-DD)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to get daily log for date: {}", date);
        DailyLogResponseDto dailyLog = dailyLogService.getOrCreateDailyLog(date);

        return ResponseEntity.ok(ApiResponse.success(dailyLog));
    }

    @PostMapping("/entries")
    @Operation(summary = "Add meal entry", description = "Adds a food item to the daily log")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> addEntry(
            @Valid @RequestBody MealEntryRequestDto request) {

        log.info("REST request to add meal entry");
        DailyLogResponseDto updatedLog = dailyLogService.addEntry(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Entry added successfully", updatedLog));
    }

    @PutMapping("/entries/{id}")
    @Operation(summary = "Update meal entry", description = "Updates an existing meal entry")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> updateEntry(
            @Parameter(description = "ID of the entry to update") @PathVariable Long id,
            @Valid @RequestBody MealEntryRequestDto request) {

        log.info("REST request to update meal entry id: {}", id);
        DailyLogResponseDto updatedLog = dailyLogService.updateEntry(id, request);

        return ResponseEntity.ok(ApiResponse.success("Entry updated successfully", updatedLog));
    }

    @DeleteMapping("/entries/{id}")
    @Operation(summary = "Delete meal entry", description = "Removes a meal entry from the log")
    public ResponseEntity<ApiResponse<DailyLogResponseDto>> deleteEntry(
            @Parameter(description = "ID of the entry to delete") @PathVariable Long id) {

        log.info("REST request to delete meal entry id: {}", id);
        DailyLogResponseDto updatedLog = dailyLogService.deleteEntry(id);

        return ResponseEntity.ok(ApiResponse.success("Entry deleted successfully", updatedLog));
    }
}
