package com.nutritiontracker.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    // Personal data
    @Column(precision = 10, scale = 2)
    private BigDecimal height; // in cm (always stored in metric)

    @Column(precision = 10, scale = 2)
    private BigDecimal weight; // in kg (always stored in metric)

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender; // MALE, FEMALE, OTHER

    // Preferences
    @Column(name = "nutritional_goal", length = 30)
    private String nutritionalGoal; // LOSE_WEIGHT, MAINTAIN, GAIN_MUSCLE, GAIN_WEIGHT

    @Column(name = "diet_type", length = 30)
    private String dietType; // STANDARD, KETOGENIC, VEGAN, etc.

    @Column(name = "activity_level", length = 20)
    private String activityLevel; // SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE, EXTRA_ACTIVE

    @Column(name = "preferred_unit_system", length = 10)
    @Builder.Default
    private String preferredUnitSystem = "METRIC"; // METRIC, IMPERIAL

    @Column(name = "preferred_language", length = 5)
    @Builder.Default
    private String preferredLanguage = "es"; // es, en

    // Calculated goals (updated when profile changes)
    @Column(name = "daily_calorie_goal", precision = 10, scale = 2)
    private BigDecimal dailyCalorieGoal;

    @Column(name = "daily_protein_goal", precision = 10, scale = 2)
    private BigDecimal dailyProteinGoal;

    @Column(name = "daily_carbs_goal", precision = 10, scale = 2)
    private BigDecimal dailyCarbsGoal;

    @Column(name = "daily_fats_goal", precision = 10, scale = 2)
    private BigDecimal dailyFatsGoal;

    // Custom macros
    @Column(name = "use_custom_macros")
    @Builder.Default
    private Boolean useCustomMacros = false;

    @Column(name = "custom_protein_percentage", precision = 5, scale = 2)
    private BigDecimal customProteinPercentage;

    @Column(name = "custom_carbs_percentage", precision = 5, scale = 2)
    private BigDecimal customCarbsPercentage;

    @Column(name = "custom_fats_percentage", precision = 5, scale = 2)
    private BigDecimal customFatsPercentage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
