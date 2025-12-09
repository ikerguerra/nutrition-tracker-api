package com.nutritiontracker.modules.food.repository;

import com.nutritiontracker.modules.food.entity.FavoriteFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteFoodRepository extends JpaRepository<FavoriteFood, Long> {

    List<FavoriteFood> findByUserId(Long userId);

    Optional<FavoriteFood> findByUserIdAndFoodId(Long userId, Long foodId);

    void deleteByUserIdAndFoodId(Long userId, Long foodId);

    boolean existsByUserIdAndFoodId(Long userId, Long foodId);
}
