package com.nutritiontracker.modules.auth.controller;

import com.nutritiontracker.modules.auth.entity.MacroPreset;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.service.MacroPresetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/macro-presets")
@RequiredArgsConstructor
public class MacroPresetController {

    private final MacroPresetService macroPresetService;

    @GetMapping
    public ResponseEntity<List<MacroPresetDTO>> getUserPresets(@AuthenticationPrincipal User user) {
        List<MacroPreset> presets = macroPresetService.getUserPresets(user.getId());
        List<MacroPresetDTO> dtos = presets.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<MacroPresetDTO> createPreset(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateMacroPresetRequest request) {
        MacroPreset preset = macroPresetService.createPreset(
                user,
                request.getName(),
                request.getProteinPercentage(),
                request.getCarbsPercentage(),
                request.getFatsPercentage());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(preset));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MacroPresetDTO> updatePreset(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CreateMacroPresetRequest request) {
        MacroPreset preset = macroPresetService.updatePreset(
                id,
                user.getId(),
                request.getName(),
                request.getProteinPercentage(),
                request.getCarbsPercentage(),
                request.getFatsPercentage());
        return ResponseEntity.ok(toDTO(preset));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePreset(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        macroPresetService.deletePreset(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/set-default")
    public ResponseEntity<MacroPresetDTO> setDefaultPreset(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        MacroPreset preset = macroPresetService.setDefaultPreset(id, user.getId());
        return ResponseEntity.ok(toDTO(preset));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<UserProfileDTO> applyPresetToProfile(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        UserProfile profile = macroPresetService.applyPresetToProfile(id, user.getId());
        return ResponseEntity.ok(toUserProfileDTO(profile));
    }

    private MacroPresetDTO toDTO(MacroPreset preset) {
        MacroPresetDTO dto = new MacroPresetDTO();
        dto.setId(preset.getId());
        dto.setName(preset.getName());
        dto.setProteinPercentage(preset.getProteinPercentage());
        dto.setCarbsPercentage(preset.getCarbsPercentage());
        dto.setFatsPercentage(preset.getFatsPercentage());
        dto.setIsDefault(preset.getIsDefault());
        dto.setCreatedAt(preset.getCreatedAt());
        return dto;
    }

    private UserProfileDTO toUserProfileDTO(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUseCustomMacros(profile.getUseCustomMacros());
        dto.setCustomProteinPercentage(
                profile.getCustomProteinPercentage() != null ? profile.getCustomProteinPercentage().intValue() : null);
        dto.setCustomCarbsPercentage(
                profile.getCustomCarbsPercentage() != null ? profile.getCustomCarbsPercentage().intValue() : null);
        dto.setCustomFatsPercentage(
                profile.getCustomFatsPercentage() != null ? profile.getCustomFatsPercentage().intValue() : null);
        return dto;
    }

    @Data
    public static class CreateMacroPresetRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must not exceed 50 characters")
        private String name;

        @NotNull(message = "Protein percentage is required")
        @Min(value = 0, message = "Protein percentage must be at least 0")
        @Max(value = 100, message = "Protein percentage must not exceed 100")
        private Integer proteinPercentage;

        @NotNull(message = "Carbs percentage is required")
        @Min(value = 0, message = "Carbs percentage must be at least 0")
        @Max(value = 100, message = "Carbs percentage must not exceed 100")
        private Integer carbsPercentage;

        @NotNull(message = "Fats percentage is required")
        @Min(value = 0, message = "Fats percentage must be at least 0")
        @Max(value = 100, message = "Fats percentage must not exceed 100")
        private Integer fatsPercentage;
    }

    @Data
    public static class MacroPresetDTO {
        private Long id;
        private String name;
        private Integer proteinPercentage;
        private Integer carbsPercentage;
        private Integer fatsPercentage;
        private Boolean isDefault;
        private java.time.LocalDateTime createdAt;
    }

    @Data
    public static class UserProfileDTO {
        private Boolean useCustomMacros;
        private Integer customProteinPercentage;
        private Integer customCarbsPercentage;
        private Integer customFatsPercentage;
    }
}
