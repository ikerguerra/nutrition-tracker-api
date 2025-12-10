package com.nutritiontracker.modules.dailylog.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyLogWeightRequestDto {

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", message = "Weight must be positive")
    @DecimalMax(value = "500.0", message = "Weight seems unrealistic")
    private BigDecimal weight;
}
