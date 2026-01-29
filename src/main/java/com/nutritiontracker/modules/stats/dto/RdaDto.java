package com.nutritiontracker.modules.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RdaDto {
    private BigDecimal fiber; // g
    private BigDecimal sugars; // g (Upper limit usually)
    private BigDecimal saturatedFats; // g (Upper limit)
    private BigDecimal sodium; // mg
    private BigDecimal calcium; // mg
    private BigDecimal iron; // mg
    private BigDecimal potassium; // mg
    private BigDecimal vitaminA; // IU or mcg (using IU to match frontend)
    private BigDecimal vitaminC; // mg
    private BigDecimal vitaminD; // IU
    private BigDecimal vitaminE; // mg
    private BigDecimal vitaminB12; // mcg
    private BigDecimal magnesium; // mg
    private BigDecimal zinc; // mg
    private BigDecimal vitaminK; // mcg
    private BigDecimal vitaminB1; // mg
    private BigDecimal vitaminB2; // mg
    private BigDecimal vitaminB3; // mg
    private BigDecimal vitaminB6; // mg
    private BigDecimal vitaminB9; // mcg (Folate)
}
