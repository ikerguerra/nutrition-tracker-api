package com.nutritiontracker.modules.mealtemplate.mapper;

import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.mealtemplate.dto.MealTemplateRequestDto;
import com.nutritiontracker.modules.mealtemplate.dto.MealTemplateResponseDto;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplate;
import com.nutritiontracker.modules.mealtemplate.entity.MealTemplateItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MealTemplateMapper {

    public MealTemplate toEntity(MealTemplateRequestDto dto, Long userId) {
        if (dto == null)
            return null;

        MealTemplate template = MealTemplate.builder()
                .userId(userId)
                .name(dto.getName())
                .description(dto.getDescription())
                .mealType(dto.getMealType())
                .isPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false)
                .build();

        if (dto.getItems() != null) {
            dto.getItems().forEach(itemDto -> {
                MealTemplateItem item = MealTemplateItem.builder()
                        .food(Food.builder().id(itemDto.getFoodId()).build())
                        .quantity(itemDto.getQuantity())
                        .unit(itemDto.getUnit())
                        .build();
                template.addItem(item);
            });
        }

        return template;
    }

    public MealTemplateResponseDto toDto(MealTemplate entity) {
        if (entity == null)
            return null;

        return MealTemplateResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .mealType(entity.getMealType())
                .isPublic(entity.getIsPublic())
                .items(entity.getItems().stream()
                        .map(this::toItemDto)
                        .collect(Collectors.toList()))
                .build();
    }

    private MealTemplateResponseDto.MealTemplateItemResponseDto toItemDto(MealTemplateItem item) {
        return MealTemplateResponseDto.MealTemplateItemResponseDto.builder()
                .id(item.getId())
                .foodId(item.getFood().getId())
                .foodName(item.getFood().getName())
                .brand(item.getFood().getBrand())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .calories(item.getFood().getNutritionalInfo().getCalories())
                .protein(item.getFood().getNutritionalInfo().getProtein())
                .carbs(item.getFood().getNutritionalInfo().getCarbohydrates())
                .fats(item.getFood().getNutritionalInfo().getFats())
                .build();
    }
}
