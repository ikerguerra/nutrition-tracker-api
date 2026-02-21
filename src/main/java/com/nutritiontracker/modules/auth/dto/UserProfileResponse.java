package com.nutritiontracker.modules.auth.dto;

import com.nutritiontracker.modules.auth.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private BigDecimal height;
    private BigDecimal weight;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private String nutritionalGoal;
    private String dietType;
    private String activityLevel;
    private String preferredUnitSystem;
    private String preferredLanguage;

    // Calculated goals
    private BigDecimal dailyCalorieGoal;
    private BigDecimal dailyProteinGoal;
    private BigDecimal dailyCarbsGoal;
    private BigDecimal dailyFatsGoal;

    // Custom macros
    private Boolean useCustomMacros;
    private BigDecimal customProteinPercentage;
    private BigDecimal customCarbsPercentage;
    private BigDecimal customFatsPercentage;

    // Gamification
    private Integer xp;
    private Integer level;

    public static UserProfileResponse fromEntity(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .dateOfBirth(profile.getDateOfBirth())
                .age(profile.getAge())
                .gender(profile.getGender())
                .nutritionalGoal(profile.getNutritionalGoal())
                .dietType(profile.getDietType())
                .activityLevel(profile.getActivityLevel())
                .preferredUnitSystem(profile.getPreferredUnitSystem())
                .preferredLanguage(profile.getPreferredLanguage())
                .dailyCalorieGoal(profile.getDailyCalorieGoal())
                .dailyProteinGoal(profile.getDailyProteinGoal())
                .dailyCarbsGoal(profile.getDailyCarbsGoal())
                .dailyFatsGoal(profile.getDailyFatsGoal())
                .useCustomMacros(profile.getUseCustomMacros())
                .customProteinPercentage(profile.getCustomProteinPercentage())
                .customCarbsPercentage(profile.getCustomCarbsPercentage())
                .customFatsPercentage(profile.getCustomFatsPercentage())
                .xp(profile.getXp())
                .level(profile.getLevel())
                .build();
    }
}
