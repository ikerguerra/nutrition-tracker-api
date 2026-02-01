package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.entity.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NutritionalCalculationService
 * Tests calorie and macro calculations without database dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Nutritional Calculation Service Tests")
class NutritionalCalculationServiceTest {

    @InjectMocks
    private NutritionalCalculationService service;

    private UserProfile maleProfile;
    private UserProfile femaleProfile;

    @BeforeEach
    void setUp() {
        // Create a typical male profile for testing
        maleProfile = UserProfile.builder()
                .weight(BigDecimal.valueOf(80)) // 80 kg
                .height(BigDecimal.valueOf(180)) // 180 cm
                .dateOfBirth(LocalDate.now().minusYears(30)) // 30 years old
                .gender("MALE")
                .activityLevel("MODERATELY_ACTIVE")
                .nutritionalGoal("MAINTAIN")
                .build();

        // Create a typical female profile for testing
        femaleProfile = UserProfile.builder()
                .weight(BigDecimal.valueOf(65)) // 65 kg
                .height(BigDecimal.valueOf(165)) // 165 cm
                .dateOfBirth(LocalDate.now().minusYears(28)) // 28 years old
                .gender("FEMALE")
                .activityLevel("LIGHTLY_ACTIVE")
                .nutritionalGoal("LOSE_WEIGHT")
                .build();
    }

    @Test
    @DisplayName("Should calculate BMR for male using Mifflin-St Jeor equation")
    void shouldCalculateBMRForMale() {
        // When
        double bmr = service.calculateBMR(maleProfile);

        // Then
        // Expected BMR for 80kg, 180cm, 30yo male ≈ 1750-1850 kcal
        assertThat(bmr).isGreaterThan(1700).isLessThan(1900);
    }

    @Test
    @DisplayName("Should calculate BMR for female using Mifflin-St Jeor equation")
    void shouldCalculateBMRForFemale() {
        // When
        double bmr = service.calculateBMR(femaleProfile);

        // Then
        // Expected BMR for 65kg, 165cm, 28yo female ≈ 1350-1450 kcal
        assertThat(bmr).isGreaterThan(1300).isLessThan(1500);
    }

    @Test
    @DisplayName("Should calculate daily calories with activity multiplier")
    void shouldCalculateDailyCaloriesWithActivityLevel() {
        // When
        double calories = service.calculateDailyCalories(maleProfile);

        // Then
        // BMR * 1.55 (moderately active) ≈ 2700-2900 kcal
        assertThat(calories).isGreaterThan(2500).isLessThan(3000);
    }

    @Test
    @DisplayName("Should reduce calories for weight loss goal")
    void shouldReduceCaloriesForWeightLoss() {
        // Given
        maleProfile.setNutritionalGoal("LOSE_WEIGHT");

        // When
        double calories = service.calculateDailyCalories(maleProfile);

        // Then
        // Should be reduced by ~500 kcal from maintenance
        assertThat(calories).isGreaterThan(2000).isLessThan(2500);
    }

    @Test
    @DisplayName("Should increase calories for muscle gain goal")
    void shouldIncreaseCaloriesForMuscleGain() {
        // Given
        maleProfile.setNutritionalGoal("GAIN_MUSCLE");

        // When
        double calories = service.calculateDailyCalories(maleProfile);

        // Then
        // Should be increased by ~300 kcal from maintenance
        assertThat(calories).isGreaterThan(2800).isLessThan(3300);
    }

    @Test
    @DisplayName("Should calculate macros with correct distribution")
    void shouldCalculateMacrosWithCorrectDistribution() {
        // Given
        double dailyCalories = 2500;

        // When
        double[] macros = service.calculateMacros(maleProfile, dailyCalories);

        // Then
        assertThat(macros).hasSize(3);

        double protein = macros[0];
        double carbs = macros[1];
        double fats = macros[2];

        // Verify all macros are positive
        assertThat(protein).isPositive();
        assertThat(carbs).isPositive();
        assertThat(fats).isPositive();

        // Verify total calories match (protein*4 + carbs*4 + fats*9)
        double totalCalories = (protein * 4) + (carbs * 4) + (fats * 9);
        assertThat(totalCalories).isCloseTo(dailyCalories, org.assertj.core.data.Offset.offset(50.0));
    }

    @Test
    @DisplayName("Should calculate higher protein for high protein diet")
    void shouldCalculateHigherProteinForHighProteinDiet() {
        // Given
        maleProfile.setDietType("HIGH_PROTEIN");
        double dailyCalories = 2500;

        // When
        double[] macros = service.calculateMacros(maleProfile, dailyCalories);

        // Then
        double protein = macros[0];
        double proteinCalories = protein * 4;
        double proteinPercentage = (proteinCalories / dailyCalories) * 100;

        // High protein diet should have >30% protein
        assertThat(proteinPercentage).isGreaterThan(30);
    }

    @Test
    @DisplayName("Should calculate very low carbs for ketogenic diet")
    void shouldCalculateVeryLowCarbsForKetogenicDiet() {
        // Given
        maleProfile.setDietType("KETOGENIC");
        double dailyCalories = 2500;

        // When
        double[] macros = service.calculateMacros(maleProfile, dailyCalories);

        // Then
        double carbs = macros[1];
        double carbCalories = carbs * 4;
        double carbPercentage = (carbCalories / dailyCalories) * 100;

        // Ketogenic diet should have <10% carbs
        assertThat(carbPercentage).isLessThan(10);
    }

    @Test
    @DisplayName("Should use custom macros when enabled")
    void shouldUseCustomMacrosWhenEnabled() {
        // Given
        maleProfile.setUseCustomMacros(true);
        maleProfile.setCustomProteinPercentage(BigDecimal.valueOf(40));
        maleProfile.setCustomCarbsPercentage(BigDecimal.valueOf(30));
        maleProfile.setCustomFatsPercentage(BigDecimal.valueOf(30));
        double dailyCalories = 2000;

        // When
        double[] macros = service.calculateMacros(maleProfile, dailyCalories);

        // Then
        double protein = macros[0];
        double carbs = macros[1];
        double fats = macros[2];

        // Verify custom percentages are applied
        assertThat(protein).isCloseTo(200, org.assertj.core.data.Offset.offset(5.0)); // 40% of 2000 = 800 cal / 4 =
                                                                                      // 200g
        assertThat(carbs).isCloseTo(150, org.assertj.core.data.Offset.offset(5.0)); // 30% of 2000 = 600 cal / 4 = 150g
        assertThat(fats).isCloseTo(67, org.assertj.core.data.Offset.offset(5.0)); // 30% of 2000 = 600 cal / 9 = 67g
    }
}
