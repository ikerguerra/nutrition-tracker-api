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
    @Column(precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(precision = 10, scale = 2)
    private BigDecimal carbohydrates;

    @Column(precision = 10, scale = 2)
    private BigDecimal fats;

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
}
