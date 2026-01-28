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
public class MealTemplateRequestDto {
    private String name;
    private String description;
    private MealType mealType;
    private Boolean isPublic;
    private List<MealTemplateItemRequestDto> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MealTemplateItemRequestDto {
        private Long foodId;
        private BigDecimal quantity;
        private String unit;
    }
}
