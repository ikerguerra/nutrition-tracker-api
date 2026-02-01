package com.nutritiontracker.modules.dailylog.dto;

import com.nutritiontracker.modules.dailylog.enums.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealEntryRequestDto {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @NotNull(message = "Food ID is required")
    private Long foodId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Unit is required")
    private String unit;

    private Long servingUnitId; // Optional, for custom serving units
}
