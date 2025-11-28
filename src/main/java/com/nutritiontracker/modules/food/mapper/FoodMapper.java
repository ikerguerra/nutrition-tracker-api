package com.nutritiontracker.modules.food.mapper;

import com.nutritiontracker.modules.food.dto.FoodRequestDto;
import com.nutritiontracker.modules.food.dto.FoodResponseDto;
import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.entity.NutritionalInfo;
import org.springframework.stereotype.Component;

@Component
public class FoodMapper {

    public Food toEntity(FoodRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Food food = Food.builder()
                .name(dto.getName())
                .brand(dto.getBrand())
                .barcode(dto.getBarcode())
                .servingSize(dto.getServingSize())
                .servingUnit(dto.getServingUnit())
                .build();

        if (dto.getNutritionalInfo() != null) {
            NutritionalInfo nutritionalInfo = toNutritionalInfoEntity(dto.getNutritionalInfo());
            food.setNutritionalInfo(nutritionalInfo);
        }

        return food;
    }

    public FoodResponseDto toDto(Food entity) {
        if (entity == null) {
            return null;
        }

        FoodResponseDto.NutritionalInfoDto nutritionalInfoDto = null;
        if (entity.getNutritionalInfo() != null) {
            nutritionalInfoDto = toNutritionalInfoDto(entity.getNutritionalInfo());
        }

        return FoodResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .brand(entity.getBrand())
                .barcode(entity.getBarcode())
                .servingSize(entity.getServingSize())
                .servingUnit(entity.getServingUnit())
                .nutritionalInfo(nutritionalInfoDto)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDto(FoodRequestDto dto, Food entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setBrand(dto.getBrand());
        entity.setBarcode(dto.getBarcode());
        entity.setServingSize(dto.getServingSize());
        entity.setServingUnit(dto.getServingUnit());

        if (dto.getNutritionalInfo() != null) {
            if (entity.getNutritionalInfo() == null) {
                NutritionalInfo nutritionalInfo = toNutritionalInfoEntity(dto.getNutritionalInfo());
                entity.setNutritionalInfo(nutritionalInfo);
            } else {
                updateNutritionalInfoFromDto(dto.getNutritionalInfo(), entity.getNutritionalInfo());
            }
        }
    }

    private NutritionalInfo toNutritionalInfoEntity(FoodRequestDto.NutritionalInfoDto dto) {
        return NutritionalInfo.builder()
                .calories(dto.getCalories())
                .protein(dto.getProtein())
                .carbohydrates(dto.getCarbohydrates())
                .fats(dto.getFats())
                .fiber(dto.getFiber())
                .sugars(dto.getSugars())
                .saturatedFats(dto.getSaturatedFats())
                .sodium(dto.getSodium())
                .calcium(dto.getCalcium())
                .iron(dto.getIron())
                .potassium(dto.getPotassium())
                .vitaminA(dto.getVitaminA())
                .vitaminC(dto.getVitaminC())
                .vitaminD(dto.getVitaminD())
                .vitaminE(dto.getVitaminE())
                .vitaminB12(dto.getVitaminB12())
                .build();
    }

    private FoodResponseDto.NutritionalInfoDto toNutritionalInfoDto(NutritionalInfo entity) {
        return FoodResponseDto.NutritionalInfoDto.builder()
                .id(entity.getId())
                .calories(entity.getCalories())
                .protein(entity.getProtein())
                .carbohydrates(entity.getCarbohydrates())
                .fats(entity.getFats())
                .fiber(entity.getFiber())
                .sugars(entity.getSugars())
                .saturatedFats(entity.getSaturatedFats())
                .sodium(entity.getSodium())
                .calcium(entity.getCalcium())
                .iron(entity.getIron())
                .potassium(entity.getPotassium())
                .vitaminA(entity.getVitaminA())
                .vitaminC(entity.getVitaminC())
                .vitaminD(entity.getVitaminD())
                .vitaminE(entity.getVitaminE())
                .vitaminB12(entity.getVitaminB12())
                .build();
    }

    private void updateNutritionalInfoFromDto(FoodRequestDto.NutritionalInfoDto dto, NutritionalInfo entity) {
        entity.setCalories(dto.getCalories());
        entity.setProtein(dto.getProtein());
        entity.setCarbohydrates(dto.getCarbohydrates());
        entity.setFats(dto.getFats());
        entity.setFiber(dto.getFiber());
        entity.setSugars(dto.getSugars());
        entity.setSaturatedFats(dto.getSaturatedFats());
        entity.setSodium(dto.getSodium());
        entity.setCalcium(dto.getCalcium());
        entity.setIron(dto.getIron());
        entity.setPotassium(dto.getPotassium());
        entity.setVitaminA(dto.getVitaminA());
        entity.setVitaminC(dto.getVitaminC());
        entity.setVitaminD(dto.getVitaminD());
        entity.setVitaminE(dto.getVitaminE());
        entity.setVitaminB12(dto.getVitaminB12());
    }
}
