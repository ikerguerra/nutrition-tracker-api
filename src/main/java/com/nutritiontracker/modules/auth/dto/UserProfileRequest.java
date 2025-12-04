package com.nutritiontracker.modules.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserProfileRequest {

    @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
    @DecimalMax(value = "300.0", message = "Height must not exceed 300 cm")
    private BigDecimal height;

    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "500.0", message = "Weight must not exceed 500 kg")
    private BigDecimal weight;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "MALE|FEMALE|OTHER", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;

    @Pattern(regexp = "LOSE_WEIGHT|MAINTAIN|GAIN_MUSCLE|GAIN_WEIGHT", message = "Invalid nutritional goal")
    private String nutritionalGoal;

    @Pattern(regexp = "STANDARD|KETOGENIC|VEGAN|VEGETARIAN|PALEO|HIGH_PROTEIN|LOW_CARB", message = "Invalid diet type")
    private String dietType;

    @Pattern(regexp = "SEDENTARY|LIGHTLY_ACTIVE|MODERATELY_ACTIVE|VERY_ACTIVE|EXTREMELY_ACTIVE", message = "Invalid activity level")
    private String activityLevel;

    @Pattern(regexp = "METRIC|IMPERIAL", message = "Unit system must be METRIC or IMPERIAL")
    private String preferredUnitSystem;

    @Pattern(regexp = "es|en", message = "Language must be 'es' or 'en'")
    private String preferredLanguage;

    private Boolean useCustomMacros;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal customProteinPercentage;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal customCarbsPercentage;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal customFatsPercentage;
}
