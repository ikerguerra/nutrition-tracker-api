package com.nutritiontracker.modules.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightDataPointDto {
    private LocalDate date;
    private Double weight;
    private Double weightChange; // Change from previous entry
    private Double movingAverage; // 7-day moving average
}
