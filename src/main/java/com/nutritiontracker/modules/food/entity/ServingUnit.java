package com.nutritiontracker.modules.food.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "serving_units")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServingUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    @Column(name = "weight_grams", nullable = false, precision = 10, scale = 2)
    private BigDecimal weightGrams;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;
}
