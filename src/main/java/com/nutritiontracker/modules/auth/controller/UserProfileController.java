package com.nutritiontracker.modules.auth.controller;

import com.nutritiontracker.modules.auth.dto.UserProfileRequest;
import com.nutritiontracker.modules.auth.dto.UserProfileResponse;
import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        UserProfile profile = userProfileService.getProfileByUserId(user.getId());
        return ResponseEntity.ok(UserProfileResponse.fromEntity(profile));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserProfileRequest request) {

        UserProfile profile = userProfileService.getProfileByUserId(user.getId());
        updateProfileFromRequest(profile, request);

        UserProfile updated = userProfileService.updateProfile(profile);
        return ResponseEntity.ok(UserProfileResponse.fromEntity(updated));
    }

    private void updateProfileFromRequest(UserProfile profile, UserProfileRequest request) {
        if (request.getHeight() != null)
            profile.setHeight(request.getHeight());
        if (request.getWeight() != null)
            profile.setWeight(request.getWeight());
        if (request.getDateOfBirth() != null)
            profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null)
            profile.setGender(request.getGender());
        if (request.getNutritionalGoal() != null)
            profile.setNutritionalGoal(request.getNutritionalGoal());
        if (request.getDietType() != null)
            profile.setDietType(request.getDietType());
        if (request.getActivityLevel() != null)
            profile.setActivityLevel(request.getActivityLevel());
        if (request.getPreferredUnitSystem() != null)
            profile.setPreferredUnitSystem(request.getPreferredUnitSystem());
        if (request.getPreferredLanguage() != null)
            profile.setPreferredLanguage(request.getPreferredLanguage());

        // Custom macros
        if (request.getUseCustomMacros() != null) {
            profile.setUseCustomMacros(request.getUseCustomMacros());
            if (request.getUseCustomMacros()) {
                profile.setCustomProteinPercentage(request.getCustomProteinPercentage());
                profile.setCustomCarbsPercentage(request.getCustomCarbsPercentage());
                profile.setCustomFatsPercentage(request.getCustomFatsPercentage());
            }
        }
    }
}
