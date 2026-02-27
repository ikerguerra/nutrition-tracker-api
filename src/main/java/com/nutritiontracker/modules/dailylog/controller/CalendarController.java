package com.nutritiontracker.modules.dailylog.controller;

import com.nutritiontracker.common.dto.ApiResponse;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.dailylog.dto.CalendarDayDto;
import com.nutritiontracker.modules.dailylog.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Calendar Management", description = "APIs for calendar summaries")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/summary")
    @Operation(summary = "Get monthly summary", description = "Retrieves daily summaries for a specific month")
    public ResponseEntity<ApiResponse<List<CalendarDayDto>>> getMonthlySummary(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Year (e.g. 2023)") @RequestParam("year") int year,
            @Parameter(description = "Month (1-12)") @RequestParam("month") int month) {

        log.info("REST request to get monthly summary for year: {}, month: {}", year, month);
        List<CalendarDayDto> summary = calendarService.getMonthlySummary(user.getId(), year, month);

        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
