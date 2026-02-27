package com.nutritiontracker.modules.mealtemplate.controller;

import com.nutritiontracker.common.dto.ApiResponse;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.dailylog.enums.MealType;
import com.nutritiontracker.modules.mealtemplate.dto.MealTemplateRequestDto;
import com.nutritiontracker.modules.mealtemplate.dto.MealTemplateResponseDto;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplate;
import com.nutritiontracker.modules.mealtemplate.mapper.MealTemplateMapper;
import com.nutritiontracker.modules.mealtemplate.service.MealTemplateService;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/meal-templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Meal Template Management", description = "APIs for managing and applying meal templates")
public class MealTemplateController {

    private final MealTemplateService mealTemplateService;
    private final MealTemplateMapper mealTemplateMapper;

    @GetMapping
    @Operation(summary = "Get user templates", description = "Retrieves all meal templates for the current user")
    public ResponseEntity<ApiResponse<List<MealTemplateResponseDto>>> getTemplates(
            @AuthenticationPrincipal User user) {

        List<MealTemplateResponseDto> templates = mealTemplateService.getUserTemplates(user.getId())
                .stream()
                .map(mealTemplateMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID", description = "Retrieves a specific meal template")
    public ResponseEntity<ApiResponse<MealTemplateResponseDto>> getTemplate(
            @PathVariable("id") Long id) {

        MealTemplate template = mealTemplateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(mealTemplateMapper.toDto(template)));
    }

    @PostMapping
    @Operation(summary = "Create template", description = "Creates a new meal template")
    public ResponseEntity<ApiResponse<MealTemplateResponseDto>> createTemplate(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody MealTemplateRequestDto request) {

        MealTemplate template = mealTemplateMapper.toEntity(request, user.getId());
        MealTemplate savedTemplate = mealTemplateService.createTemplate(template);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Template created successfully", mealTemplateMapper.toDto(savedTemplate)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update template", description = "Updates an existing meal template")
    public ResponseEntity<ApiResponse<MealTemplateResponseDto>> updateTemplate(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long id,
            @Valid @RequestBody MealTemplateRequestDto request) {

        MealTemplate template = mealTemplateMapper.toEntity(request, user.getId());
        MealTemplate updatedTemplate = mealTemplateService.updateTemplate(id, template, user.getId());

        return ResponseEntity
                .ok(ApiResponse.success("Template updated successfully", mealTemplateMapper.toDto(updatedTemplate)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete template", description = "Removes a meal template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long id) {

        mealTemplateService.deleteTemplate(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully", null));
    }

    @PostMapping("/{id}/apply")
    @Operation(summary = "Apply template", description = "Adds all foods from a template to the daily log of a specific date")
    public ResponseEntity<ApiResponse<Void>> applyTemplate(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long id,
            @Parameter(description = "Date (YYYY-MM-DD)") @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Meal type override") @RequestParam(required = false) MealType mealType) {

        mealTemplateService.applyTemplate(id, date, mealType, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Template applied successfully", null));
    }
}
