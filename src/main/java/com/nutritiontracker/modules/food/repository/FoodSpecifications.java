package com.nutritiontracker.modules.food.repository;

import com.nutritiontracker.modules.food.entity.Food;
import com.nutritiontracker.modules.food.enums.FoodCategory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FoodSpecifications {

    public static Specification<Food> withFilters(
            String query,
            FoodCategory category,
            BigDecimal minCalories, BigDecimal maxCalories,
            BigDecimal minProtein, BigDecimal maxProtein,
            BigDecimal minCarbs, BigDecimal maxCarbs,
            BigDecimal minFats, BigDecimal maxFats) {

        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Text Search (Name or Brand)
            if (StringUtils.hasText(query)) {
                String likePattern = "%" + query.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
                Predicate brandLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), likePattern);
                predicates.add(criteriaBuilder.or(nameLike, brandLike));
            }

            // Category Filter
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // Nutritional Range Filters (Joined with NutritionalInfo)
            // Assuming Food has OneToOne 'nutritionalInfo'

            // Calories
            if (minCalories != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("nutritionalInfo").get("calories"), minCalories));
            }
            if (maxCalories != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("nutritionalInfo").get("calories"), maxCalories));
            }

            // Protein
            if (minProtein != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(root.get("nutritionalInfo").get("protein"), minProtein));
            }
            if (maxProtein != null) {
                predicates
                        .add(criteriaBuilder.lessThanOrEqualTo(root.get("nutritionalInfo").get("protein"), maxProtein));
            }

            // Carbs
            if (minCarbs != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("nutritionalInfo").get("carbohydrates"),
                        minCarbs));
            }
            if (maxCarbs != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(root.get("nutritionalInfo").get("carbohydrates"), maxCarbs));
            }

            // Fats
            if (minFats != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("nutritionalInfo").get("fat"), minFats));
            }
            if (maxFats != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("nutritionalInfo").get("fat"), maxFats));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
