package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.enums.ActivityLevel;
import com.nutritiontracker.modules.auth.enums.Gender;
import com.nutritiontracker.modules.auth.enums.NutritionalGoal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for calculating nutritional goals and caloric needs.
 * Uses Mifflin-St Jeor equation for BMR and Harris-Benedict for TDEE.
 */
@Service
@Slf4j
public class NutritionalCalculationService {

    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor equation.
     * Men: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age + 5
     * Women: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age - 161
     */
    public double calculateBMR(UserProfile profile) {
        if (profile.getWeight() == null || profile.getHeight() == null || profile.getAge() == null) {
            throw new IllegalArgumentException("Weight, height, and age are required for BMR calculation");
        }

        double weight = profile.getWeight().doubleValue();
        double height = profile.getHeight().doubleValue();
        int age = profile.getAge();
        Gender gender = Gender.valueOf(profile.getGender());

        double bmr = 10 * weight + 6.25 * height - 5 * age;

        if (gender == Gender.MALE) {
            bmr += 5;
        } else {
            bmr -= 161;
        }

        log.debug("Calculated BMR for user: {} kcal/day", bmr);
        return bmr;
    }

    /**
     * Calculate Total Daily Energy Expenditure (TDEE).
     * TDEE = BMR × Activity Factor
     */
    public double calculateTDEE(UserProfile profile) {
        double bmr = calculateBMR(profile);
        double activityMultiplier = getActivityMultiplier(
                ActivityLevel.valueOf(profile.getActivityLevel()));

        double tdee = bmr * activityMultiplier;
        log.debug("Calculated TDEE for user: {} kcal/day", tdee);
        return tdee;
    }

    /**
     * Calculate daily caloric goal based on nutritional objective.
     * - MAINTAIN: TDEE
     * - LOSE_WEIGHT: TDEE - 500 kcal (approx 0.5kg/week)
     * - GAIN_MUSCLE: TDEE + 300 kcal (lean bulk)
     * - GAIN_WEIGHT: TDEE + 500 kcal
     */
    public double calculateDailyCalories(UserProfile profile) {
        double tdee = calculateTDEE(profile);
        NutritionalGoal goal = NutritionalGoal.valueOf(profile.getNutritionalGoal());

        double dailyCalories = switch (goal) {
            case LOSE_WEIGHT -> tdee - 500;
            case GAIN_MUSCLE -> tdee + 300;
            case GAIN_WEIGHT -> tdee + 500;
            case MAINTAIN -> tdee;
        };

        log.debug("Calculated daily calories for goal {}: {} kcal", goal, dailyCalories);
        return Math.round(dailyCalories);
    }

    /**
     * Get activity level multiplier for TDEE calculation.
     */
    private double getActivityMultiplier(ActivityLevel level) {
        return switch (level) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
            case EXTREMELY_ACTIVE -> 1.9;
        };
    }

    /**
     * Calculate macronutrient distribution in grams.
     * Returns array: [protein, carbs, fat]
     */
    public double[] calculateMacros(UserProfile profile, double dailyCalories) {
        double proteinPercentage = profile.getProteinPercentage() != null
                ? profile.getProteinPercentage()
                : 20.0;
        double carbsPercentage = profile.getCarbsPercentage() != null
                ? profile.getCarbsPercentage()
                : 50.0;
        double fatPercentage = profile.getFatPercentage() != null
                ? profile.getFatPercentage()
                : 30.0;

        // Calories per gram: Protein=4, Carbs=4, Fat=9
        double proteinGrams = (dailyCalories * proteinPercentage / 100) / 4;
        double carbsGrams = (dailyCalories * carbsPercentage / 100) / 4;
        double fatGrams = (dailyCalories * fatPercentage / 100) / 9;

        log.debug("Calculated macros - P: {}g, C: {}g, F: {}g",
                Math.round(proteinGrams), Math.round(carbsGrams), Math.round(fatGrams));

        return new double[] {
                Math.round(proteinGrams),
                Math.round(carbsGrams),
                Math.round(fatGrams)
        };
    }
}
