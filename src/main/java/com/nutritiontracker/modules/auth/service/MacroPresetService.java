package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.entity.MacroPreset;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.repository.MacroPresetRepository;
import com.nutritiontracker.modules.auth.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MacroPresetService {

    private final MacroPresetRepository macroPresetRepository;
    private final UserProfileRepository userProfileRepository;

    public List<MacroPreset> getUserPresets(Long userId) {
        return macroPresetRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public MacroPreset createPreset(User user, String name, Integer proteinPercentage, Integer carbsPercentage,
            Integer fatsPercentage) {
        // Validate sum
        if (proteinPercentage + carbsPercentage + fatsPercentage != 100) {
            throw new IllegalArgumentException("Macro percentages must sum to 100");
        }

        // Check for duplicate name
        if (macroPresetRepository.existsByUserIdAndName(user.getId(), name)) {
            throw new IllegalArgumentException("A preset with this name already exists");
        }

        MacroPreset preset = MacroPreset.builder()
                .user(user)
                .name(name)
                .proteinPercentage(proteinPercentage)
                .carbsPercentage(carbsPercentage)
                .fatsPercentage(fatsPercentage)
                .isDefault(false)
                .build();

        return macroPresetRepository.save(preset);
    }

    @Transactional
    public MacroPreset updatePreset(Long presetId, Long userId, String name, Integer proteinPercentage,
            Integer carbsPercentage, Integer fatsPercentage) {
        MacroPreset preset = macroPresetRepository.findByIdAndUserId(presetId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

        // Validate sum
        if (proteinPercentage + carbsPercentage + fatsPercentage != 100) {
            throw new IllegalArgumentException("Macro percentages must sum to 100");
        }

        // Check for duplicate name (excluding current preset)
        if (!preset.getName().equals(name) && macroPresetRepository.existsByUserIdAndName(userId, name)) {
            throw new IllegalArgumentException("A preset with this name already exists");
        }

        preset.setName(name);
        preset.setProteinPercentage(proteinPercentage);
        preset.setCarbsPercentage(carbsPercentage);
        preset.setFatsPercentage(fatsPercentage);

        return macroPresetRepository.save(preset);
    }

    @Transactional
    public void deletePreset(Long presetId, Long userId) {
        MacroPreset preset = macroPresetRepository.findByIdAndUserId(presetId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

        macroPresetRepository.delete(preset);
    }

    @Transactional
    public MacroPreset setDefaultPreset(Long presetId, Long userId) {
        MacroPreset preset = macroPresetRepository.findByIdAndUserId(presetId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

        // Unset all other defaults for this user
        List<MacroPreset> userPresets = macroPresetRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (MacroPreset p : userPresets) {
            if (p.getIsDefault()) {
                p.setIsDefault(false);
                macroPresetRepository.save(p);
            }
        }

        // Set this preset as default
        preset.setIsDefault(true);
        return macroPresetRepository.save(preset);
    }

    @Transactional
    public UserProfile applyPresetToProfile(Long presetId, Long userId) {
        MacroPreset preset = macroPresetRepository.findByIdAndUserId(presetId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Preset not found"));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));

        profile.setUseCustomMacros(true);
        profile.setCustomProteinPercentage(java.math.BigDecimal.valueOf(preset.getProteinPercentage()));
        profile.setCustomCarbsPercentage(java.math.BigDecimal.valueOf(preset.getCarbsPercentage()));
        profile.setCustomFatsPercentage(java.math.BigDecimal.valueOf(preset.getFatsPercentage()));

        return userProfileRepository.save(profile);
    }
}
