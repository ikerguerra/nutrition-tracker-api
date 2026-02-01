package com.nutritiontracker.modules.auth.service;

import com.nutritiontracker.modules.auth.entity.User;
import com.nutritiontracker.modules.auth.entity.UserProfile;
import com.nutritiontracker.modules.auth.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserProfileService
 * Tests profile management and goal recalculation logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Profile Service Tests")
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private NutritionalCalculationService nutritionalCalculationService;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testProfile = UserProfile.builder()
                .id(1L)
                .user(testUser)
                .weight(BigDecimal.valueOf(80))
                .height(BigDecimal.valueOf(180))
                .dateOfBirth(LocalDate.now().minusYears(30))
                .gender("MALE")
                .activityLevel("MODERATELY_ACTIVE")
                .nutritionalGoal("MAINTAIN")
                .preferredUnitSystem("METRIC")
                .preferredLanguage("es")
                .useCustomMacros(false)
                .build();
    }

    @Test
    @DisplayName("Should create default profile for new user")
    void shouldCreateDefaultProfileForNewUser() {
        // Given
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        // When
        UserProfile created = userProfileService.createDefaultProfile(testUser);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getUser()).isEqualTo(testUser);
        assertThat(created.getPreferredUnitSystem()).isEqualTo("METRIC");
        assertThat(created.getPreferredLanguage()).isEqualTo("es");
        assertThat(created.getUseCustomMacros()).isFalse();

        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("Should recalculate goals when weight changes")
    void shouldRecalculateGoalsWhenWeightChanges() {
        // Given
        testProfile.setWeight(BigDecimal.valueOf(75)); // Changed from 80 to 75

        when(nutritionalCalculationService.calculateDailyCalories(testProfile)).thenReturn(2500.0);
        when(nutritionalCalculationService.calculateMacros(testProfile, 2500.0))
                .thenReturn(new double[] { 150.0, 250.0, 80.0 });
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        // When
        UserProfile updated = userProfileService.updateProfile(testProfile);

        // Then
        assertThat(updated.getDailyCalorieGoal()).isEqualTo(BigDecimal.valueOf(2500.0));
        assertThat(updated.getDailyProteinGoal()).isEqualTo(BigDecimal.valueOf(150.0));
        assertThat(updated.getDailyCarbsGoal()).isEqualTo(BigDecimal.valueOf(250.0));
        assertThat(updated.getDailyFatsGoal()).isEqualTo(BigDecimal.valueOf(80.0));

        verify(nutritionalCalculationService).calculateDailyCalories(testProfile);
        verify(nutritionalCalculationService).calculateMacros(testProfile, 2500.0);
        verify(userProfileRepository).save(testProfile);
    }

    @Test
    @DisplayName("Should recalculate goals when activity level changes")
    void shouldRecalculateGoalsWhenActivityLevelChanges() {
        // Given
        testProfile.setActivityLevel("VERY_ACTIVE");

        when(nutritionalCalculationService.calculateDailyCalories(testProfile)).thenReturn(3000.0);
        when(nutritionalCalculationService.calculateMacros(testProfile, 3000.0))
                .thenReturn(new double[] { 180.0, 300.0, 100.0 });
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        // When
        UserProfile updated = userProfileService.updateProfile(testProfile);

        // Then
        assertThat(updated.getDailyCalorieGoal()).isEqualTo(BigDecimal.valueOf(3000.0));
        verify(nutritionalCalculationService).calculateDailyCalories(testProfile);
    }

    @Test
    @DisplayName("Should recalculate goals when nutritional goal changes")
    void shouldRecalculateGoalsWhenNutritionalGoalChanges() {
        // Given
        testProfile.setNutritionalGoal("LOSE_WEIGHT");

        when(nutritionalCalculationService.calculateDailyCalories(testProfile)).thenReturn(2000.0);
        when(nutritionalCalculationService.calculateMacros(testProfile, 2000.0))
                .thenReturn(new double[] { 160.0, 180.0, 70.0 });
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        // When
        UserProfile updated = userProfileService.updateProfile(testProfile);

        // Then
        assertThat(updated.getDailyCalorieGoal()).isEqualTo(BigDecimal.valueOf(2000.0));
        verify(nutritionalCalculationService).calculateDailyCalories(testProfile);
    }

    @Test
    @DisplayName("Should NOT recalculate goals when only language changes")
    void shouldNotRecalculateGoalsWhenOnlyLanguageChanges() {
        // Given
        testProfile.setWeight(null); // Remove nutritional data
        testProfile.setPreferredLanguage("en");

        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArgument(0));

        // When
        userProfileService.updateProfile(testProfile);

        // Then
        verify(nutritionalCalculationService, never()).calculateDailyCalories(any());
        verify(nutritionalCalculationService, never()).calculateMacros(any(), anyDouble());
        verify(userProfileRepository).save(testProfile);
    }

    @Test
    @DisplayName("Should get profile by user ID")
    void shouldGetProfileByUserId() {
        // Given
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        // When
        UserProfile found = userProfileService.getProfileByUserId(1L);

        // Then
        assertThat(found).isEqualTo(testProfile);
        verify(userProfileRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Should throw exception when profile not found")
    void shouldThrowExceptionWhenProfileNotFound() {
        // Given
        when(userProfileRepository.findByUserId(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userProfileService.getProfileByUserId(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User profile not found");
    }
}
