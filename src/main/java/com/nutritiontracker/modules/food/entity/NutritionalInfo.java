package com.nutritiontracker.modules.food.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "nutritional_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false, unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Food food;

    // Macronutrients (per serving)
    @Column(precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal calories = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal protein = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal carbohydrates = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal fats = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal fiber;

    @Column(precision = 10, scale = 2)
    private BigDecimal sugars;

    @Column(name = "saturated_fats", precision = 10, scale = 2)
    private BigDecimal saturatedFats;

    // Micronutrients (per serving) - in mg unless specified
    @Column(precision = 10, scale = 2)
    private BigDecimal sodium;

    @Column(precision = 10, scale = 2)
    private BigDecimal calcium;

    @Column(precision = 10, scale = 2)
    private BigDecimal iron;

    @Column(precision = 10, scale = 2)
    private BigDecimal potassium;

    @Column(name = "vitamin_a", precision = 10, scale = 2)
    private BigDecimal vitaminA;

    @Column(name = "vitamin_c", precision = 10, scale = 2)
    private BigDecimal vitaminC;

    @Column(name = "vitamin_d", precision = 10, scale = 2)
    private BigDecimal vitaminD;

    @Column(name = "vitamin_e", precision = 10, scale = 2)
    private BigDecimal vitaminE;

    @Column(name = "vitamin_b12", precision = 10, scale = 2)
    private BigDecimal vitaminB12;

    @Column(precision = 10, scale = 2)
    private BigDecimal magnesium;

    @Column(precision = 10, scale = 2)
    private BigDecimal zinc;

    @Column(name = "vitamin_k", precision = 10, scale = 2)
    private BigDecimal vitaminK;

    @Column(name = "vitamin_b1", precision = 10, scale = 2)
    private BigDecimal vitaminB1; // Thiamine

    @Column(name = "vitamin_b2", precision = 10, scale = 2)
    private BigDecimal vitaminB2; // Riboflavin

    @Column(name = "vitamin_b3", precision = 10, scale = 2)
    private BigDecimal vitaminB3; // Niacin

    @Column(name = "vitamin_b6", precision = 10, scale = 2)
    private BigDecimal vitaminB6;

    @Column(name = "vitamin_b9", precision = 10, scale = 2)
    private BigDecimal vitaminB9; // Folate
}
