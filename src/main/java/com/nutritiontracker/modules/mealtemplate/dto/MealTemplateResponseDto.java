package com.nutritiontracker.modules.mealtemplate.dto;

import com.nutritiontracker.modules.dailylog.enums.MealType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealTemplateResponseDto {
    private Long id;
    private String name;
    private String description;
    private MealType mealType;
    private Boolean isPublic;
    private List<MealTemplateItemResponseDto> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MealTemplateItemResponseDto {
        private Long id;
        private Long foodId;
        private String foodName;
        private String brand;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fats;
    }
}
