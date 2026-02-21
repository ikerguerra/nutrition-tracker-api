package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final NutritionalCalculationService nutritionalCalculationService;

    @Transactional
    public UserProfile createDefaultProfile(User user) {
        UserProfile profile = UserProfile.builder()
                .user(user)
                .preferredUnitSystem("METRIC")
                .preferredLanguage("es")
                .useCustomMacros(false)
                .build();

        return userProfileRepository.save(profile);
    }

    @Transactional
    public UserProfile updateProfile(UserProfile profile) {
        // Recalculate nutritional goals if relevant data changed
        if (hasNutritionalData(profile)) {
            calculateAndSetGoals(profile);
        }

        return userProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public UserProfile getProfileByUserId(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User profile not found"));
    }

    private boolean hasNutritionalData(UserProfile profile) {
        return profile.getWeight() != null
                && profile.getHeight() != null
                && profile.getDateOfBirth() != null
                && profile.getGender() != null
                && profile.getActivityLevel() != null
                && profile.getNutritionalGoal() != null;
    }

    private void calculateAndSetGoals(UserProfile profile) {
        double dailyCalories = nutritionalCalculationService.calculateDailyCalories(profile);
        double[] macros = nutritionalCalculationService.calculateMacros(profile, dailyCalories);

        profile.setDailyCalorieGoal(BigDecimal.valueOf(dailyCalories));
        profile.setDailyProteinGoal(BigDecimal.valueOf(macros[0]));
        profile.setDailyCarbsGoal(BigDecimal.valueOf(macros[1]));
        profile.setDailyFatsGoal(BigDecimal.valueOf(macros[2]));
    }

    @Transactional
    public UserProfile addXp(Long userId, int xpAmount) {
        UserProfile profile = getProfileByUserId(userId);

        int currentXp = profile.getXp() != null ? profile.getXp() : 0;
        currentXp += xpAmount;
        profile.setXp(currentXp);

        // Level calculation based on XP: Level = floor(sqrt(xp / 100)) + 1
        int calculatedLevel = (int) Math.floor(Math.sqrt((double) currentXp / 100)) + 1;

        if (calculatedLevel > (profile.getLevel() != null ? profile.getLevel() : 1)) {
            profile.setLevel(calculatedLevel);
        }

        return userProfileRepository.save(profile);
    }
}
